import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin, interval, merge, startWith, switchMap } from 'rxjs';
import { GroupBookingService, apiErrorMessage } from '../core/group-booking.service';
import { AuthService } from '../core/auth.service';
import { AuditEvent, GroupBooking, Participant } from '../core/models';

/** Backstop only - live updates normally arrive over SSE well before this fires. */
const FALLBACK_POLL_MS = 20000;

@Component({
  selector: 'app-group-booking-detail',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './group-booking-detail.html',
  styleUrl: './group-booking-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GroupBookingDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly groupBookingService = inject(GroupBookingService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly bookingId = this.route.snapshot.paramMap.get('id')!;
  /** Set when this page was opened from someone else's invite link (?ref=<theirParticipantId>). */
  private readonly referrerParticipantId = this.route.snapshot.queryParamMap.get('ref');
  protected readonly auth = inject(AuthService);
  protected readonly linkCopied = signal(false);

  protected readonly booking = signal<GroupBooking | null>(null);
  protected readonly auditTrail = signal<AuditEvent[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly joining = signal(false);
  protected readonly joinError = signal<string | null>(null);
  protected readonly leaving = signal(false);
  protected readonly leaveError = signal<string | null>(null);
  protected readonly joiningWaitlist = signal(false);
  protected readonly joinWaitlistError = signal<string | null>(null);
  protected readonly leavingWaitlist = signal(false);
  protected readonly leaveWaitlistError = signal<string | null>(null);

  /** The server computes this from the auth token on every response, including GET. */
  protected readonly myParticipantId = computed(() => this.booking()?.myParticipantId ?? null);
  protected readonly myWaitlistEntryId = computed(() => this.booking()?.myWaitlistEntryId ?? null);
  protected readonly myWaitlistPosition = computed(() => {
    const entryId = this.myWaitlistEntryId();
    if (!entryId) {
      return null;
    }
    const index = (this.booking()?.waitlist ?? []).findIndex((entry) => entry.id === entryId);
    return index === -1 ? null : index + 1;
  });

  /** Newest first, for a history feed you read top-down. */
  protected readonly auditTrailNewestFirst = computed(() => [...this.auditTrail()].reverse());

  protected readonly referralSavings = computed(() => {
    const booking = this.booking();
    if (!booking || booking.myPricePerSeat === null) {
      return 0;
    }
    return booking.currentPricePerSeat - booking.myPricePerSeat;
  });

  protected readonly inviteLink = computed(() => {
    const participantId = this.myParticipantId();
    return participantId
      ? `${location.origin}/group-bookings/${this.bookingId}?ref=${encodeURIComponent(participantId)}`
      : null;
  });

  constructor() {
    // Live updates arrive over SSE (near-instant); the periodic timer is just a
    // backstop in case a connection is dropped and the browser hasn't reconnected yet.
    merge(
      interval(FALLBACK_POLL_MS).pipe(startWith(0)),
      this.groupBookingService.streamEvents(this.bookingId),
    )
      .pipe(
        switchMap(() =>
          forkJoin({
            booking: this.groupBookingService.getGroupBooking(this.bookingId),
            auditTrail: this.groupBookingService.getAuditTrail(this.bookingId),
          }),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ booking, auditTrail }) => {
          this.booking.set(booking);
          this.auditTrail.set(auditTrail);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Group booking not found.');
          this.loading.set(false);
        },
      });
  }

  protected join(): void {
    this.joining.set(true);
    this.joinError.set(null);
    this.groupBookingService.joinGroupBooking(this.bookingId, this.referrerParticipantId).subscribe({
      next: (booking) => {
        this.booking.set(booking);
        this.joining.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.joining.set(false);
        this.joinError.set(apiErrorMessage(err, 'Could not join this group.'));
      },
    });
  }

  protected copyInviteLink(): void {
    const link = this.inviteLink();
    if (!link) {
      return;
    }
    navigator.clipboard.writeText(link).then(() => {
      this.linkCopied.set(true);
      setTimeout(() => this.linkCopied.set(false), 2000);
    });
  }

  protected leave(): void {
    this.leaving.set(true);
    this.leaveError.set(null);
    this.groupBookingService.leaveGroupBooking(this.bookingId).subscribe({
      next: (booking) => {
        this.booking.set(booking);
        this.leaving.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.leaving.set(false);
        this.leaveError.set(apiErrorMessage(err, 'Could not leave this group.'));
      },
    });
  }

  protected joinWaitlist(): void {
    this.joiningWaitlist.set(true);
    this.joinWaitlistError.set(null);
    this.groupBookingService.joinWaitlist(this.bookingId).subscribe({
      next: (booking) => {
        this.booking.set(booking);
        this.joiningWaitlist.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.joiningWaitlist.set(false);
        this.joinWaitlistError.set(apiErrorMessage(err, 'Could not join the waitlist.'));
      },
    });
  }

  protected leaveWaitlist(): void {
    this.leavingWaitlist.set(true);
    this.leaveWaitlistError.set(null);
    this.groupBookingService.leaveWaitlist(this.bookingId).subscribe({
      next: (booking) => {
        this.booking.set(booking);
        this.leavingWaitlist.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.leavingWaitlist.set(false);
        this.leaveWaitlistError.set(apiErrorMessage(err, 'Could not leave the waitlist.'));
      },
    });
  }

  protected referrerName(participant: Participant, booking: GroupBooking): string | null {
    if (!participant.referredByParticipantId) {
      return null;
    }
    return booking.participants.find((p) => p.id === participant.referredByParticipantId)?.customerName ?? null;
  }

  protected seatsRemaining(booking: GroupBooking): number {
    return booking.maxParticipants - booking.participantCount;
  }

  protected seatsUntilNextTier(booking: GroupBooking): number | null {
    const nextTier = booking.priceTiers.find((tier) => tier.minParticipants > booking.participantCount);
    return nextTier ? nextTier.minParticipants - booking.participantCount : null;
  }

  protected describeEvent(event: AuditEvent): string {
    switch (event.type) {
      case 'PARTICIPANT_JOINED':
        return `${event.customerName} joined (${event.participantCount} in the group)`;
      case 'PARTICIPANT_LEFT':
        return `Someone left (${event.participantCount} in the group)`;
      case 'FINALIZED':
        return event.status === 'CONFIRMED'
          ? `Group confirmed with ${event.participantCount} traveler${event.participantCount === 1 ? '' : 's'}`
          : 'Group cancelled - not enough travelers joined in time';
    }
  }
}
