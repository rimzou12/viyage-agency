import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { TripService } from '../core/trip.service';
import { GroupBookingService, apiErrorMessage } from '../core/group-booking.service';
import { Trip } from '../core/models';

@Component({
  selector: 'app-trip-detail',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './trip-detail.html',
  styleUrl: './trip-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TripDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly tripService = inject(TripService);
  private readonly groupBookingService = inject(GroupBookingService);

  protected readonly trip = signal<Trip | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly creating = signal(false);
  protected readonly createError = signal<string | null>(null);

  constructor() {
    const tripId = this.route.snapshot.paramMap.get('id')!;
    this.tripService.getTrip(tripId).subscribe({
      next: (trip) => {
        this.trip.set(trip);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Trip not found.');
        this.loading.set(false);
      },
    });
  }

  protected startGroup(customerName: string): void {
    const trip = this.trip();
    if (!trip || !customerName.trim()) {
      return;
    }
    this.creating.set(true);
    this.createError.set(null);
    this.groupBookingService.createGroupBooking(trip.id, customerName.trim()).subscribe({
      next: (booking) => this.router.navigate(['/group-bookings', booking.id]),
      error: (err: HttpErrorResponse) => {
        this.creating.set(false);
        this.createError.set(apiErrorMessage(err, 'Could not start a group booking.'));
      },
    });
  }
}
