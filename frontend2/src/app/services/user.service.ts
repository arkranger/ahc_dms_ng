import { Injectable } from '@angular/core';
import { User } from '../models/user.model';
import { HttpClient } from '@angular/common/http';
import baseUrl from './helper';
import { Observable } from 'rxjs';

// @Injectable signals that the service should be created and injected via Angular’s DIs.
// Additionally, we need to specify which provider we’ll use for creating and injecting 
// the UserService class. Otherwise, Angular won’t be able to inject it into t
// he component classes
@Injectable({ 
  // makes the service a singleton throughout the application.
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }


  //add user

  public addUser(user: User) {
    return this.http.post(`${baseUrl}/user/`, user);
  }

  public findAll(): Observable<User[]> {
    return this.http.get<User[]>(`${baseUrl}/user/`);
  }


}
