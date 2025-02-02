import { Component, OnInit } from "@angular/core";
import { AuthService } from "../../Service/auth.service";
import { StorageService } from "../../Service/storage.service";
import { User } from "../../Model/User";
import { LoginRequest } from "../../Model/LoginRequest";

@Component({
  selector: "login-modal",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.css"],
})
export class LoginComponent implements OnInit {
  isLoggedIn = false;
  // todo: Throw errors when login fails
  loginFailed = false;
  error = "";
  loginRequest: LoginRequest;
  checker = false;
  typedCode = "";
  code = "";

  constructor(
    private authService: AuthService,
    private storageService: StorageService
  ) {
    this.loginRequest = new LoginRequest();
  }

  ngOnInit(): void {
    if (this.storageService.isLoggedIn()) {
      this.isLoggedIn = true;
    }
  }

  onSubmit(): void {
    this.authService.login(this.loginRequest).subscribe({
      next: (value) => {
        this.storageService.saveUser(value);
        this.isLoggedIn = true;
        this.code = this.storageService.getCode();
      },
      error: () => {
        this.loginFailed = true;
        window.alert("Falsches Passwort oder Email!");
        window.location.reload();
      },
    });
    if (!this.loginFailed) {
      this.switchChecker();
    }
  }

  switchChecker(): void {
    this.checker = !this.checker;
  }
  verifyCode(): void {
    this.authService.verify(this.code, this.typedCode);
  }
  isVerified(): boolean {
    return this.authService.isVerified();
  }
}
