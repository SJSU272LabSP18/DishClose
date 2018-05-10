//DB Config
var mongoose = require('mongoose');
mongoose.connect('mongodb://root:root@ds135700.mlab.com:35700/dishclose')
var db = mongoose.connection;
db.on('error', console.error.bind(console, 'MongoDB connection error:'));

db.once('connected', function () {
  return console.log('Successfully connected to  MongoDB Database');
});

db.once('disconnected', function () {
  return console.error('Successfully disconnected from MongoDB Database');
});

var autoIncrement = require('mongoose-auto-increment');
autoIncrement.initialize(db);
exports.autoIncrement = autoIncrement; 
exports.db = db;




