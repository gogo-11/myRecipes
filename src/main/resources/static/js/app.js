function showMenu() {
      var menu = document.getElementById("small-menu");
      if (menu.className === "show") {
        menu.className = "";
      } else {
        menu.className = "show";
      }
    }

    const headerContainer = document.querySelector('#user-account');
	const header = document.querySelector('header');

	// Add an event listener to the window for scrolling
	window.addEventListener('scroll', function() {
	  // If the user has scrolled more than 60px, add the scrolled class
	  if (window.pageYOffset > 60) {
		headerContainer.classList.add('scrolled');
	  } else {
		// Otherwise, remove the scrolled class
		headerContainer.classList.remove('scrolled');
	  }
	});

	function matchPassword() {
      var pw1 = document.getElementById("pass").value;
      var pw2 = document.getElementById("cPass").value;
      console.log(pw1);
      console.log(pw2);
      if(pw1 == pw2)
      {
        document.getElementById("subm").disabled=false;
        document.getElementById("pass").style.borderColor = "black";
        document.getElementById("cPass").style.borderColor = "black";
            document.getElementById("Message").innerHTML = "";
      } else {
        document.getElementById("pass").style.borderColor = "tomato";
        document.getElementById("cPass").style.borderColor = "tomato";
        document.getElementById("subm").disabled=true;
        document.getElementById("Message").innerHTML = "Паролите не съвпадат!";
      }
    }

    function showPassword() {
       var x = document.getElementById("pass");
         if (x.type === "password") {
             x.type = "text";
         } else {
            x.type = "password";
         }
    }