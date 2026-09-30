import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { TripService } from '../core/trip.service';
import { Trip } from '../core/models';
import { tripPhotoUrls } from '../core/photos';
import { ImageCarousel } from '../shared/image-carousel/image-carousel';

@Component({
  selector: 'app-trip-list',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, DatePipe, ImageCarousel],
  templateUrl: './trip-list.html',
  styleUrl: './trip-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TripList {
  private readonly tripService = inject(TripService);

  protected readonly trips = signal<Trip[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.tripService.listTrips().subscribe({
      next: (trips) => {
        this.trips.set(trips);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load trips. Is the backend running on localhost:8080?');
        this.loading.set(false);
      },
    });
  }

  protected startingPrice(trip: Trip): number {
    const cheapestTier = trip.priceTiers.at(-1);
    return cheapestTier ? cheapestTier.pricePerSeat : trip.basePrice;
  }

  private readonly photosByTripId = new Map<string, string[]>();

  protected photos(trip: Trip): string[] {
    let photos = this.photosByTripId.get(trip.id);
    if (!photos) {
      photos = tripPhotoUrls(trip.id, 4);
      this.photosByTripId.set(trip.id, photos);
    }
    return photos;
  }
}
