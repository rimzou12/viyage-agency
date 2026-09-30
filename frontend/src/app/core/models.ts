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

export interface WaitlistEntry {
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
  /** Which participant (if any) belongs to the caller, computed from the auth token. */
  myParticipantId: string | null;
  /** Oldest-waiting first. */
  waitlist: WaitlistEntry[];
  /** Which waitlist entry (if any) belongs to the caller, computed from the auth token. */
  myWaitlistEntryId: string | null;
}

export type AuditEventType = 'PARTICIPANT_JOINED' | 'PARTICIPANT_LEFT' | 'FINALIZED';

export interface AuditEvent {
  type: AuditEventType;
  participantId: string | null;
  customerName: string | null;
  participantCount: number;
  pricePerSeat: number;
  /** Only set for a FINALIZED entry. */
  status: GroupBookingStatus | null;
  occurredAt: string;
}

export interface User {
  id: string;
  email: string;
  displayName: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}
