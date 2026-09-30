export interface PriceTier {
  minParticipants: number;
  pricePerSeat: number;
}

export interface Trip {
  id: string;
  destination: string;
  description: string;
  departureDate: string;
  returnDate: string;
  minParticipants: number;
  maxParticipants: number;
  bookingDeadline: string;
  basePrice: number;
  priceTiers: PriceTier[];
}

export type GroupBookingStatus = 'OPEN' | 'CONFIRMED' | 'CANCELLED';

export interface Participant {
  id: string;
  customerName: string;
  joinedAt: string;
}

export interface GroupBooking {
  id: string;
  tripId: string;
  status: GroupBookingStatus;
  participantCount: number;
  minParticipants: number;
  maxParticipants: number;
  currentPricePerSeat: number;
  deadline: string;
  priceTiers: PriceTier[];
  participants: Participant[];
}
