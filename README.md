<h1>Easy Permission Manager</h1> It is a lightweight Android library for handling runtime permissions effortlessly. It simplifies permission requests, checks, and callbacks with minimal code, ensuring a smooth user experience<br><br>

<b>Step 1</b>. Add it in your root 
settings.gradle at the end of repositories:
    
    repositories 
    {
	  maven { url 'https://jitpack.io' }
    }

<b>Step 2</b>. Add the dependency

   	dependencies {
	    implementation 'com.github.arshadiqbal268:PermissionManager:1.0'
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
