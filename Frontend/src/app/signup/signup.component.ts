import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { SnackbarService } from '../services/snackbar.service';
import { MatDialogRef } from '@angular/material/dialog';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { error } from 'console';
import { GlobalConstants } from '../shared/global-constants';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss']
})
export class SignupComponent implements OnInit {
  password = true;
  confirmPassword = true;
  signupForm: any = FormGroup;
  responseMessage: any = "";

  constructor(private formBuilder: FormBuilder,
    private router: Router,
    private userService: UserService,
    private snackbarService: SnackbarService,
    public dialogRef: MatDialogRef<SignupComponent>,
    private ngxService: NgxUiLoaderService
  ) { }

  ngOnInit(): void {
    this.signupForm = this.formBuilder.group({
      username: [null, [Validators.required]],
      // email:[null , Validators.required],
      contactNumber: [null, [Validators.required, Validators.pattern(GlobalConstants.contactRegex)]],
      password: [null, Validators.required],
      confirmPassword: [null, [Validators.required]],
      // phonenumber:[null , [Validators.required]],
      email: [null, Validators.required],
    })
  }

  validateSubmit() {
    if (this.signupForm.controls['password'].value != this.signupForm.controls['confirmPassword'].value) {
      return true;
    } else {
      return false;
    }
  }

  handleSubmit() {
    this.ngxService.start();
    var formDate = this.signupForm.value;
    var data = {
      username: formDate.username,
      email: formDate.email,
      contactNumber: formDate.contactNumber,
      password: formDate.password,
      phonenumber: formDate.phonenumber,
    }

    this.userService.signup(data).subscribe((response: any) => {
      console.log(response)
      this.ngxService.stop();
      this.responseMessage = response?.message;
      this.snackbarService.openSnackBar(this.responseMessage, "success");
      this.dialogRef.close();
      this.router.navigate(['/']);
    }, (error) => {
      this.ngxService.stop();
      if (error.error?.message) {
        this.responseMessage = error.error?.message;
      }
      else {
        this.responseMessage = GlobalConstants.genericError;
      }
      this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
    })
  }

}
