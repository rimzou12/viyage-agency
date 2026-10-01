import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { API_BASE_URL } from './api-config';
import { AuthResponse } from './models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.removeItem('agency-voyage:auth');
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.removeItem('agency-voyage:auth');
  });

  it('starts logged out when there is nothing in storage', () => {
    expect(service.isLoggedIn()).toBeFalsy();
    expect(service.currentUser()).toBeNull();
  });

  it('logging in stores the token and user, and marks the service as logged in', () => {
    service.login('alice@example.com', 'password123').subscribe();

    httpMock.expectOne(`${API_BASE_URL}/api/auth/login`).flush(sampleAuthResponse());

    expect(service.isLoggedIn()).toBeTruthy();
    expect(service.currentUser()?.displayName).toBe('Alice');
    expect(service.token).toBe('jwt-token');
    expect(localStorage.getItem('agency-voyage:auth')).toContain('jwt-token');
  });

  it('registering stores the token and user just like logging in', () => {
    service.register('alice@example.com', 'password123', 'Alice').subscribe();

    httpMock.expectOne(`${API_BASE_URL}/api/auth/register`).flush(sampleAuthResponse());

    expect(service.isLoggedIn()).toBeTruthy();
  });

  it('logout clears the stored session', () => {
    service.login('alice@example.com', 'password123').subscribe();
    httpMock.expectOne(`${API_BASE_URL}/api/auth/login`).flush(sampleAuthResponse());
    expect(service.isLoggedIn()).toBeTruthy();

    service.logout();

    expect(service.isLoggedIn()).toBeFalsy();
    expect(service.token).toBeNull();
    expect(localStorage.getItem('agency-voyage:auth')).toBeNull();
  });

  function sampleAuthResponse(): AuthResponse {
    return {
      token: 'jwt-token',
      user: { id: 'u1', email: 'alice@example.com', displayName: 'Alice', isAdmin: false },
    };
  }
});
