import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { TripService } from '../core/trip.service';
import { GroupBookingService, apiErrorMessage } from '../core/group-booking.service';
import { AuthService } from '../core/auth.service';
import { Trip } from '../core/models';
import { tripPhotoUrls } from '../core/photos';
import { ImageCarousel } from '../shared/image-carousel/image-carousel';

@Component({
  selector: 'app-trip-detail',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, DatePipe, ImageCarousel],
  templateUrl: './trip-detail.html',
  styleUrl: './trip-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TripDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly tripService = inject(TripService);
  private readonly groupBookingService = inject(GroupBookingService);
  protected readonly auth = inject(AuthService);

  protected readonly trip = signal<Trip | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly creating = signal(false);
  protected readonly createError = signal<string | null>(null);
  protected readonly photos = computed(() => {
    const trip = this.trip();
    return trip ? tripPhotoUrls(trip.id, 6) : [];
  });

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

  protected startGroup(): void {
    const trip = this.trip();
    if (!trip) {
      return;
    }
    this.creating.set(true);
    this.createError.set(null);
    this.groupBookingService.createGroupBooking(trip.id).subscribe({
      next: (booking) => this.router.navigate(['/group-bookings', booking.id]),
      error: (err: HttpErrorResponse) => {
        this.creating.set(false);
        this.createError.set(apiErrorMessage(err, 'Could not start a group booking.'));
      },
    });
  }
}
