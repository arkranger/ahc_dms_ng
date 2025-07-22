import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from 'src/app/models/user.model';
import { UserService } from 'src/app/services/user.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {

  constructor(
    private userService: UserService,
    private snack: MatSnackBar
  ) { }

  user = new User();

  formSubmit() {
    // add service: userService
    if (this.user.username == '' || this.user.password == '') {
      this.snack.open("Username and Password are required", 'Close', {
        duration: 3000,
        verticalPosition: 'top',
        horizontalPosition: 'right'
      });
      return;
    }
    this.userService.addUser(this.user).subscribe(
      (data: any) => {
        // success
        console.log(data);
        Swal.fire('Success', 'User is registered successfully with id : ' + data.id, 'success');
        this.user = new User(); // reset user object
      },
      (error) => {
        // error
        Swal.fire('Error', 'Something went wrong in registration', 'error');
        console.log(error);
      }
    )
  }

}
