<h1>Easy Permission Manager</h1> It is a lightweight Android library for handling runtime permissions effortlessly. It simplifies permission requests, checks, and callbacks with minimal code, ensuring a smooth user experience<br><br>

<b>Step 1</b>. Add it in your root 
settings.gradle at the end of repositories:
    
    repositories 
    {
	  maven { url 'https://jitpack.io' }
    }

<b>Step 2</b>. Add the dependency

   	dependencies {
	    implementation 'com.github.arshadiqbal268:PermissionManager:$latest-version'
   	}

<br>



	   val permissionManager = PermissionManager(this@MainActivity)
    
       permissionManager.requestPermission(READ_MEDIA_IMAGES, "Image",
                { isPermissionAllowed ->
                    
                }, true,
                { isPermissionAllowed ->
                    // rational permission case
                    
                }
            )

<br><br><br>

  # Android Permission Manager Usage Guide

A simple table explaining key parameters of an Android Permission Manager. <br><b>Mandatory parameters are marked with (✅), while optional parameters can be omitted</b>

| Parameter            | Description       |
|----------------------|------------------|
| ✅ Permission  |  Which Permission        |
| ✅ PermissionName  |  Name of Permission that would be shown on Dialog  |
| ✅ onPermissionResult  |  A callback function that gets invoked when the permission request completes.<br> If the permission is granted, it receives true<br> If the permission is denied, it receives false	 |
| ✅ showExplanationDialog  |  if true, it shows an explanation dialog before requesting permissions        |
| ✅ onRationalPermissionResultCallback  |  A callback function in case of Rational that gets invoked when the permission request completes.<br> If the permission is granted, it receives true<br> If the permission is denied, it receives false	 |
| dialogPermissionExplanationTitle  |  `String? (Optional)`  <br>A custom title for the explanation dialog before requesting permission  <br> If provided, this title will be shown on Dialog<br>  If not provided, a default title will be used      |
| dialogPermissionExplanationDescription  |  `String? (Optional)`  <br>A custom Description for the explanation dialog before requesting permission  <br> If provided, this Description will be shown on Dialog<br>  If not provided, a default description will be used      |
| dialogTitleRational  |  `String? (Optional)`  <br>A custom title for the Rational dialog  <br> If provided, this title will be shown on Dialog<br>  If not provided, a default title will be used      |
| dialogDescriptionRational  |  `String? (Optional)`  <br>A custom Description for the Rational dialog <br> If provided, this Description will be shown on Dialog<br>  If not provided, a default description will be used  |
| onDeniedButtonOfExplanationDialog  |  `(Optional)` <br> This is an optional callback that gets triggered when the cancel button is pressed of the explanation dialog. |

<br><br>
# Permission Manager - Dialog Customization (Optional)
  Hexadecimal color code 

| Parameter                  | Description            |
|----------------------------|------------------------|
| `dialogBgColor`            | Background color (#ffffff) |
| `dialogTitleTextColor`     | Title text color    |
| `dialogDescriptionTextColor` | Description text color |
| `dialogButtonBgColor`      | Button background color |
| `dialogButtonTextColor`    | Button text color      |
| `dialogCancelButtonColor`  | Cancel button color    |


<br><br>

# Contributing 
Contributions are welcome!
<br><br>

# Contact 
For questions or feedback, please contact me at (mailto:arshadiqbal268@gmail.com).
<br><br>

# Custom Application Development
If you are interested in developing a customised application with specific functionalities, please feel free to reach out. I offer tailored solutions to meet your unique requirements. Contact me at arshadiqbal268@gmail.com to discuss your project needs.



