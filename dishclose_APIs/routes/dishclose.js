var express = require('express');
var request = require('request');
var unirest = require('unirest');
var router = express.Router();
let fs = require('fs');
var Feedback = require('../feedback');

router.get('/', function (req, res, next) {
    res.send('respond with a resource');
});

router.post('/processImageWithMicrosoft', function (req, res, next) {
    // Process Image API

    const filePath = './images/test.jpg';
    let binary = fs.createReadStream(filePath);
    request.post({
        url: 'https://southcentralus.api.cognitive.microsoft.com/customvision/v1.1/Prediction/2efa0452-7912-48cb-ad21-5d9c3d648937/image',
        headers: {
            'Content-Type': 'application/octet-stream',
            'Prediction-key': '7bca9ed9f6ad49d7a59faaac7e4f8b00',
        },
        body: binary
    }, function (error, response, body) {
        if (response.statusCode === 200) {
            var jsonData = (JSON.parse(response.body));
            if ('Predictions' in jsonData) {
                console.log('Successfull response from microsoft')
                let predictions = jsonData.Predictions;
                if (predictions.length > 0) {
                    console.log('Successfully predicted by Custom Vision')
                    //Response of Custom Vision Service Goes Here
                    let customVisionTag = predictions[0].Tag
                    console.log('customVisionTag : ' + customVisionTag)
                    let food2fork = 'http://food2fork.com/api/search?key=6b1d745dbc9cc67fcfc7dedeed65ac51&q='
                    //let requestUrl = food2fork + encodeURIComponent(customVisionTag);
                    //let requestUrl = food2fork + "apple%20pie";
                    //let requestUrl = food2fork + "bruschetta";
                    //let requestUrl = food2fork + "club%20sandwich";
                    //let requestUrl = food2fork + "falafel";
                    let requestUrl = food2fork + "samosa";

                    request.post({
                        url: requestUrl,
                    }, function (error, response) {
                        if (response.statusCode === 200) {
                            console.log('Successfull response from food2fork')
                            var jsonData = (JSON.parse(response.body));
                            if ('count' in jsonData && jsonData.count > 0) {

                                var recipes = jsonData.recipes;
                                var processItems = function (x) {
                                    console.log("Iteration :" + x)
                                    if (x < recipes.length) {
                                        var recipeDetail = jsonData.recipes[x];
                                        let recipeURL = recipeDetail.source_url;
                                        console.log("URL : " + recipeURL)
                                        let recipeAPI = 'https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/extract?forceExtraction=false&url=';
                                        let finalCall = recipeAPI + encodeURIComponent(recipeURL);
                                        unirest.get(finalCall)
                                            .header("X-Mashape-Key", "jDXWt6doJEmshXwhDnk6kX5UHdGXp1PqPQ6jsnrr82pi2xVjvF")
                                            .end(function (mshapeResponse) {
                                                if (mshapeResponse.statusCode === 200) {
                                                    var result = (JSON.parse(mshapeResponse.raw_body));
                                                    console.log('Successfull response from mshape API')
                                                    let ingredients = []
                                                    if ('extendedIngredients' in result) {
                                                        console.log('Ingredients found');
                                                        let extendedIngredients = result.extendedIngredients;
                                                        extendedIngredients.forEach(element => {
                                                            ingredients.push(
                                                                {
                                                                    name: element.originalString,
                                                                    image: element.image
                                                                })
                                                        });
                                                    }
                                                    console.log(ingredients)
                                                    let steps = []
                                                    if ('analyzedInstructions' in result && result.analyzedInstructions.length > 0) {
                                                        console.log('Instructions found');
                                                        console.log(result.analyzedInstructions)
                                                        steps = result.analyzedInstructions[0].steps;
                                                        res.json({
                                                            success: true,
                                                            ingredients: ingredients,
                                                            error: false,
                                                            steps: steps,
                                                            result: result
                                                        })
                                                        return;
                                                    } else {
                                                        processItems(x + 1);
                                                    }
                                                } else {
                                                    res.json({
                                                        success: false,
                                                        error: true,
                                                        message: 'mshape failed'
                                                    })
                                                }
                                            });
                                    }
                                };
                                processItems(0);
                            }
                            else {
                                res.json({
                                    error: true,
                                    message: 'No recipes found'
                                })
                            }
                        } else {
                            res.json({
                                error: true,
                                message: 'food2fork failed'
                            })
                        }
                    });
                }
            }
        }
    });
    return;
});

function groupBy(collection, property) {
    var i = 0, val, index,
        values = [], result = [];
    for (; i < collection.length; i++) {
        val = collection[i][property];
        index = values.indexOf(val);
        if (index > -1)
            result[index].push(collection[i]);
        else {
            values.push(val);
            result.push([collection[i]]);
        }
    }
    return result;
}

router.post('/adminDashboard', function (req, res, next) {
    // Handle Feedback API
    console.log("Admin Dashboard API")

    Feedback.find(function (error, feedbacks) {
        // In case of any error return
        try {

            let result=[];
            if (error) throw error;
            var groups = groupBy(feedbacks, "tag");

            groups.forEach((element,index) => {
                let imageYesCount=0;
                let recipeYesCount=0;
                let ingredientYesCount=0;
                let tag=''
                element.forEach(feedback => {
                    tag=feedback.tag;
                    if(feedback.image=='Yes'){
                        imageYesCount=imageYesCount+1;
                    }
                    if(feedback.ingredient=='Yes'){
                        ingredientYesCount=ingredientYesCount+1;
                    }
                    if(feedback.recipe=='Yes'){
                        recipeYesCount=recipeYesCount+1;
                    }
                });
                var finalObject={
                    tag:tag,
                    total:element.length,
                    image:imageYesCount,
                    recipe:recipeYesCount,
                    ingredient:ingredientYesCount
                }
                result.push(finalObject);

            });

            var nodemailer = require('nodemailer');

            var transporter = nodemailer.createTransport({
              service: 'gmail',
              auth: {
                user: 'freelancer.prototype@gmail.com',
                pass: '!QAZ2wsx#EDC'
              }
            });
            
            var mailOptions = { 
              from: 'Dish Close',
              to: 'dishclose@yahoo.com',
              subject: 'DishClose Daily Report',
              text: JSON.stringify(result)
            };
            
            transporter.sendMail(mailOptions, function(error, info){
              if (error) {
                console.log(error);
              } else {
                console.log('Email sent: ' + info.response);
              }
            });

            res.json(result);          
            return;
        } catch (error) {
            console.log('Catch : ' + error.message);
            res.json("Admin Dashboard failed");
            return;
        }
    });

});


router.post('/handleFeedback', function (req, res, next) {
    // Handle Feedback API
    console.log("Handle Feedback API called")
    console.log("Parameters :")
    console.log(req.body);
    let tag = req.body.tag;
    let image = req.body.image;
    let text = req.body.text;
    let ingredient = req.body.ingredient;
    let recipe = req.body.recipe;
    var feedback = new Feedback();

    if (tag && image && text && recipe && ingredient) {
        
        feedback.tag = tag;
        feedback.image = image;
        feedback.text = text;
        feedback.ingredient = ingredient;
        feedback.recipe = recipe;
        //console.log(feedback);

        feedback.save(function (err, user) {
            if (err) {
                console.log('Error in Saving project: ' + err);
                res.json("Error adding feedback.")
            }
            console.log("Feedback Added");
            res.json("Feedback Added")
            return;
        });

    }
    else {
        console.log('No Data Provided');
        res.json("No Data Provided")

    }

});

router.post('/processTag', function (req, res, next) {
    // Process Image API
    console.log("processTag API called")
    console.log("Parameters :")
    console.log(req.body);
    if (req.body.tag) {
        //Response of Custom Vision Service Goes Here
        let tag = req.body.tag
        console.log('Custom Vision Predicted Tag : ' + tag)
        let food2fork = 'http://food2fork.com/api/search?key=6b1d745dbc9cc67fcfc7dedeed65ac51&q='
        //let requestUrl = food2fork + encodeURIComponent(customVisionTag);
        //let requestUrl = food2fork + "apple%20pie";
        //let requestUrl = food2fork + "bruschetta";
        //let requestUrl = food2fork + "club%20sandwich";
        //let requestUrl = food2fork + "falafel";
        let requestUrl = food2fork + encodeURIComponent(tag);

        request.post({
            url: requestUrl,
        }, function (error, response) {
            if (response.statusCode === 200) {
                console.log('Successfull response from food2fork')
                var jsonData = (JSON.parse(response.body));
                if ('count' in jsonData && jsonData.count > 0) {

                    var recipes = jsonData.recipes;
                    var processItems = function (x) {
                        console.log("Iteration :" + x)
                        if (x < recipes.length) {
                            var recipeDetail = jsonData.recipes[x];
                            let recipeURL = recipeDetail.source_url;
                            console.log("URL : " + recipeURL)
                            let recipeAPI = 'https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/extract?forceExtraction=false&url=';
                            let finalCall = recipeAPI + encodeURIComponent(recipeURL);
                            unirest.get(finalCall)
                                .header("X-Mashape-Key", "jDXWt6doJEmshXwhDnk6kX5UHdGXp1PqPQ6jsnrr82pi2xVjvF")
                                .end(function (mshapeResponse) {
                                    if (mshapeResponse.statusCode === 200) {
                                        var result = (JSON.parse(mshapeResponse.raw_body));
                                        console.log('Successfull response from mshape API')
                                        let ingredients = []
                                        if ('extendedIngredients' in result) {
                                            console.log('Ingredients found');
                                            let extendedIngredients = result.extendedIngredients;
                                            extendedIngredients.forEach(element => {
                                                ingredients.push(element.originalString)
                                            });
                                        }
                                        console.log(ingredients)
                                        let steps = []
                                        if ('analyzedInstructions' in result && result.analyzedInstructions.length > 0) {

                                            console.log('Instructions found');
                                            console.log(result.analyzedInstructions)

                                            result.analyzedInstructions[0].steps.forEach(element => {
                                                steps.push(element.step)
                                            });
                                            res.json({
                                                title: recipeDetail.title,
                                                success: true,
                                                ingredients: ingredients,
                                                error: false,
                                                steps: steps,
                                                mshape: result,
                                                food2fork: jsonData
                                            })
                                            return;
                                        } else {
                                            processItems(x + 1);
                                        }
                                    } else {
                                        res.json({
                                            success: false,
                                            error: true,
                                            message: 'mshape failed'
                                        })
                                    }
                                });
                        }
                    };
                    processItems(0);
                }
                else {
                    res.json({
                        error: true,
                        message: 'No recipes found'
                    })
                }
            } else {
                res.json({
                    error: true,
                    message: 'food2fork failed'
                })
            }
        });
    } else {
        res.json({
            error: true,
            message: 'No tag provided'
        })
    }
    return;
});
module.exports = router;
