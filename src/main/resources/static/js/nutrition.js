document.addEventListener('DOMContentLoaded', function () {
    var nutritionContainer = document.getElementById('nutrition-estimate');
    if (!nutritionContainer) {
        return;
    }

    var recipeId = nutritionContainer.getAttribute('data-recipe-id');
    if (!recipeId) {
        return;
    }

    fetch('/api/v1/recipes/' + recipeId + '/nutrition')
        .then(function (response) {
            if (response.status === 204) {
                return generateNutrition(recipeId);
            }
            if (!response.ok) {
                throw new Error('Nutrition request failed');
            }
            return response.json();
        })
        .then(function (nutrition) {
            if (!nutrition) {
                return;
            }

            setText('nutrition-calories', nutrition.calories);
            setText('nutrition-protein', nutrition.proteinGrams);
            setText('nutrition-carbohydrates', nutrition.carbohydratesGrams);
            setText('nutrition-fat', nutrition.fatGrams);
            setText('nutrition-notes', nutrition.notes);
            nutritionContainer.style.display = '';
        })
        .catch(function () {
            nutritionContainer.style.display = 'none';
        });
});

function setText(elementId, value) {
    var element = document.getElementById(elementId);
    if (!element || value === null || value === undefined) {
        return;
    }

    element.textContent = value;
}

function generateNutrition(recipeId) {
    return fetch('/api/v1/recipes/' + recipeId + '/nutrition', {
        method: 'POST'
    }).then(function (response) {
        if (!response.ok) {
            throw new Error('Nutrition generation failed');
        }
        return response.json();
    });
}
