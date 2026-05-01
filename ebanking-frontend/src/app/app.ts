import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './components/navbar/navbar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar],
  styleUrl: './app.css',
  template: `<app-navbar />
    <div class="container mt-4">
      <router-outlet />
    </div> `,
})
export class App {}
