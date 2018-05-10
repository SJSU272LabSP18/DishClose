'use strict';
//import dependency
var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var mongoServices = require('./mongo');
var autoIncrement = mongoServices.autoIncrement;
//create new instance of the mongoose.schema. the schema takes an 
//object that shows the shape of your database entries.

var FeedbackSchema = new Schema({
    tag:{type: String},
    text: { type: String },
    ingredient: { type: String },
    image: { type: String },
    recipe: { type: String },
});

//export our module to use in server.js
FeedbackSchema.plugin(autoIncrement.plugin, 'Feedback');
module.exports = mongoose.model('Feedback', FeedbackSchema);