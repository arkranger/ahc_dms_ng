import { Component } from '@angular/core';
import { User } from 'src/app/models/user.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  user = new User();

  loginSubmit() { 
    // Logic for login submission will go here
    console.log("Login form submitted");
  }

}
