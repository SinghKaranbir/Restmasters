/**
 * Created by karanbir on 15/11/15.
 */
var mongoose = require('mongoose');
var Schema = mongoose.Schema,
    ObjectId = Schema.ObjectId;
var bcrypt = require('bcrypt-nodejs');


var userSchema = new mongoose.Schema({
    email: String,
    firstName: String,
    lastName: String,
    password: String,
    userType:String,
    regId: String,
    courses : [String],
    created_at: {type: Date, default: Date.now}
});

var courseSchema = new mongoose.Schema({
    courseId : String,
    courseName : String
});

var attendanceSchema = new mongoose.Schema({
    email : String,
    course_id : String,
    attended_on : {type: Date, default: Date.now}
});




userSchema.pre('save', function(next) {
    var user = this;
    var SALT_FACTOR = 5;

    if (!user.isModified('password')) return next();

    bcrypt.genSalt(SALT_FACTOR, function(err, salt) {
        if (err) return next(err);

        bcrypt.hash(user.password, salt, null, function(err, hash) {
            if (err) return next(err);
            user.password = hash;
            next();
        });
    });
});

userSchema.methods.comparePassword = function(candidatePassword, cb) {
    bcrypt.compare(candidatePassword, this.password, function(err, isMatch) {
        if (err) return cb(err);
        cb(null, isMatch);
    });
};

mongoose.model('User', userSchema);
mongoose.model('Course', courseSchema);
mongoose.model('Attendance', attendanceSchema);
