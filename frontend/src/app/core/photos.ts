/**
 * Deterministic per-trip photo set (same trip id always yields the same photos).
 * There's no real destination-photography API wired up yet - picsum.photos is used
 * as a stand-in so every trip gets a stable, good-looking set of images.
 */
export function tripPhotoUrls(tripId: string, count = 5): string[] {
  return Array.from({ length: count }, (_, i) => `https://picsum.photos/seed/${tripId}-${i}/1000/620`);
}
