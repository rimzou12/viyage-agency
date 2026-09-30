import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { TripService } from '../core/trip.service';
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
  private readonly tripService = inject(TripService);

  protected readonly trip = signal<Trip | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

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
}
