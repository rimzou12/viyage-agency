import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { interval, merge, startWith, switchMap } from 'rxjs';
import { GroupBookingService, apiErrorMessage } from '../core/group-booking.service';
import { GroupBooking } from '../core/models';

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

  protected readonly booking = signal<GroupBooking | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly joining = signal(false);
  protected readonly joinError = signal<string | null>(null);
  protected readonly leaving = signal(false);
  protected readonly leaveError = signal<string | null>(null);
  protected readonly myParticipantId = signal<string | null>(
    this.groupBookingService.myParticipantId(this.bookingId),
  );

  constructor() {
    // Live updates arrive over SSE (near-instant); the periodic timer is just a
    // backstop in case a connection is dropped and the browser hasn't reconnected yet.
    merge(
      interval(FALLBACK_POLL_MS).pipe(startWith(0)),
      this.groupBookingService.streamEvents(this.bookingId),
    )
      .pipe(
        switchMap(() => this.groupBookingService.getGroupBooking(this.bookingId)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (booking) => {
          this.booking.set(booking);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Group booking not found.');
          this.loading.set(false);
        },
      });
  }

  protected join(customerName: string): void {
    if (!customerName.trim()) {
      return;
    }
    this.joining.set(true);
    this.joinError.set(null);
    this.groupBookingService.joinGroupBooking(this.bookingId, customerName.trim()).subscribe({
      next: (booking) => {
        this.booking.set(booking);
        this.joining.set(false);
        if (booking.myParticipantId) {
          this.groupBookingService.rememberMyParticipantId(this.bookingId, booking.myParticipantId);
          this.myParticipantId.set(booking.myParticipantId);
        }
      },
      error: (err: HttpErrorResponse) => {
        this.joining.set(false);
        this.joinError.set(apiErrorMessage(err, 'Could not join this group.'));
      },
    });
  }

  protected leave(): void {
    const participantId = this.myParticipantId();
    if (!participantId) {
      return;
    }
    this.leaving.set(true);
    this.leaveError.set(null);
    this.groupBookingService.leaveGroupBooking(this.bookingId, participantId).subscribe({
      next: (booking) => {
        this.booking.set(booking);
        this.leaving.set(false);
        this.groupBookingService.forgetMyParticipantId(this.bookingId);
        this.myParticipantId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        this.leaving.set(false);
        this.leaveError.set(apiErrorMessage(err, 'Could not leave this group.'));
      },
    });
  }

  protected seatsRemaining(booking: GroupBooking): number {
    return booking.maxParticipants - booking.participantCount;
  }

  protected seatsUntilNextTier(booking: GroupBooking): number | null {
    const nextTier = booking.priceTiers.find((tier) => tier.minParticipants > booking.participantCount);
    return nextTier ? nextTier.minParticipants - booking.participantCount : null;
  }
}
