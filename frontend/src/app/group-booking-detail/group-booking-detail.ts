import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { interval, startWith, switchMap } from 'rxjs';
import { GroupBookingService, apiErrorMessage } from '../core/group-booking.service';
import { GroupBooking } from '../core/models';

const POLL_INTERVAL_MS = 4000;

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

  constructor() {
    interval(POLL_INTERVAL_MS)
      .pipe(
        startWith(0),
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
      },
      error: (err: HttpErrorResponse) => {
        this.joining.set(false);
        this.joinError.set(apiErrorMessage(err, 'Could not join this group.'));
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
