import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';

@Component({
  selector: 'app-image-carousel',
  standalone: true,
  templateUrl: './image-carousel.html',
  styleUrl: './image-carousel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '(mouseenter)': 'pause()',
    '(mouseleave)': 'resume()',
  },
})
export class ImageCarousel {
  readonly images = input.required<string[]>();
  readonly autoplayMs = input(4500);

  private readonly destroyRef = inject(DestroyRef);
  private timer: ReturnType<typeof setInterval> | null = null;

  protected readonly index = signal(0);
  protected readonly indices = computed(() => this.images().map((_, i) => i));

  constructor() {
    this.destroyRef.onDestroy(() => this.stopTimer());
    // (Re)starts autoplay whenever the image set changes; also fires once at creation.
    effect(() => {
      this.images();
      this.autoplayMs();
      this.resume();
    });
  }

  protected next(event?: Event): void {
    this.stopEvent(event);
    const total = this.images().length;
    this.index.set((this.index() + 1) % total);
  }

  protected previous(event?: Event): void {
    this.stopEvent(event);
    const total = this.images().length;
    this.index.set((this.index() - 1 + total) % total);
  }

  protected goTo(i: number, event?: Event): void {
    this.stopEvent(event);
    this.index.set(i);
  }

  /** Stops a nav/dot click from also activating an ancestor link (e.g. a trip card). */
  private stopEvent(event?: Event): void {
    event?.preventDefault();
    event?.stopPropagation();
  }

  protected pause(): void {
    this.stopTimer();
  }

  protected resume(): void {
    this.stopTimer();
    if (this.images().length > 1) {
      this.timer = setInterval(() => this.next(), this.autoplayMs());
    }
  }

  private stopTimer(): void {
    if (this.timer !== null) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }
}
