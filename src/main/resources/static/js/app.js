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

	window.addEventListener('scroll', function() {
	  if (window.pageYOffset > 60) {
		headerContainer.classList.add('scrolled');
	  } else {
		headerContainer.classList.remove('scrolled');
	  }
	});

	function matchPassword() {
      var pw1 = document.getElementById("pass").value;
      var pw2 = document.getElementById("cPass").value;
      console.log(pw1);
      console.log(pw2);

      if (pw1.length < 6 && pw1 !== ""){
        document.getElementById("subm").disabled=true;
        document.getElementById("Message2").innerHTML = "Паролата трябва да е поне 6 символа!";
      } else {
        document.getElementById("Message2").innerHTML = "";
      }

      if(pw1 == pw2)
      {
        //document.getElementById("submitPassword").disabled = false;
        document.getElementById("pass").style.borderColor = "black";
        document.getElementById("cPass").style.borderColor = "black";
        document.getElementById("subm").disabled=false;
        document.getElementById("Message").innerHTML = "";
      } else {
        document.getElementById("pass").style.borderColor = "tomato";
        document.getElementById("cPass").style.borderColor = "tomato";
        document.getElementById("subm").disabled=true;
        //document.getElementById("submitPassword").disabled = true;
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
    {
    const recipeName = document.getElementById("recipeName");
    const portions = document.getElementById("portions");
    const cookingTime = document.getElementById("cookingTime");
    const cookingSteps = document.getElementById("cookingSteps");
    const category = document.getElementById("category");
    const products = document.getElementById("products");
    const image = document.getElementById("image");
    const button = document.getElementById("button");

        function checkInputs() {
              if (recipeName.value !== recipeName.defaultValue ||
                  portions.value !== portions.defaultValue ||
                  cookingTime.value !== cookingTime.defaultValue ||
                  cookingSteps.value !== cookingSteps.defaultValue ||
                  category.value !== category.defaultValue ||
                  (image.value !== null && image.value !== image.defaultValue) ||
                  products.value !== products.defaultValue) {
                button.disabled = false;
              } else {
                button.disabled = true;
              }
            };

    recipeName.addEventListener("input", checkInputs);
    portions.addEventListener("input", checkInputs);
    cookingTime.addEventListener("input", checkInputs);
    cookingSteps.addEventListener("input", checkInputs);
    category.addEventListener("input", checkInputs);
    products.addEventListener("input", checkInputs);
    image.addEventListener("input", checkInputs);
    }

