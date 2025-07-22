import { Injectable } from '@angular/core';
import { delay, Observable, of, throwError } from 'rxjs';

export interface User {
  id: number;
  name: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private currentUser: User | null = null;

  login(email: string, password: string): Observable<User> {
    // Mock authentication - replace with actual HTTP call
    if (email === 'test@example.com' && password === 'password') {
      const user: User = {
        id: 1,
        name: 'Test User',
        email: email
      };
      this.currentUser = user;
      return of(user).pipe(delay(1000)); // Simulate network delay
    } else {
      return throwError(() => new Error('Invalid credentials'));
    }
  }

  logout(): void {
    this.currentUser = null;
  }

  getCurrentUser(): User | null {
    return this.currentUser;
  }

  isLoggedIn(): boolean {
    return this.currentUser !== null;
  }
}
