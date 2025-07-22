import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { Navbar } from './layouts/navbar/navbar';
import { Footer } from './layouts/footer/footer';
import { UserService } from './services/user.service';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, Navbar, Footer],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  title = 'frontend';
  currentRoute: string = '';
  isLoading = false;

  constructor(
    private router: Router,
    private userService: UserService
  ) {
    // Track route changes
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.currentRoute = event.urlAfterRedirects;
        this.updatePageTitle();
      });
  }

  ngOnInit() {
    this.initializeApp();
  }

  private initializeApp() {
    this.isLoading = true;
    
    // Simulate app initialization
    setTimeout(() => {
      this.isLoading = false;
      console.log('Angular 20 App initialized successfully');
    }, 1000);
  }

  private updatePageTitle() {
    let title = 'Frontend App';
    
    switch (this.currentRoute) {
      case '/home':
        title = 'Home - Frontend App';
        break;
      case '/login':
        title = 'Login - Frontend App';
        break;
      default:
        title = 'Frontend App';
    }
    
    document.title = title;
  }

  get isUserLoggedIn(): boolean {
    return this.userService.isLoggedIn();
  }

  get currentUser() {
    return this.userService.getCurrentUser();
  }

  onLogout() {
    this.userService.logout();
    this.router.navigate(['/login']);
  }
}
