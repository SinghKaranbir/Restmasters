var mongoose = require('mongoose');
var User = mongoose.model('User');
var LocalStrategy   = require('passport-local').Strategy;


module.exports = function(passport){

    // Passport needs to be able to serialize and deserialize users to support persistent login sessions
    passport.serializeUser(function(user, done) {
        done(null, user.id);
    });

    passport.deserializeUser(function(id, done) {
        User.findById(id, function(err, user) {
            done(err, user);
        });
    });

    passport.use('login', new LocalStrategy({
            // by default, local strategy uses username and password, we will override with email
            usernameField : 'email',
            passwordField : 'password',
            passReqToCallback : true
        },
        function(req, email, password, done) {
            // check in mongo if a user with username exists or not
            User.findOne({ 'email' :  email },
                function(err, user) {
                    // In case of any error, return using the done method
                    if (err)
                        return done(err);
                    // Username does not exist, log the error and redirect back
                    if (!user){
                        console.log('User Not Found with username '+ email);
                        return done(null, false, {message: 'Incorrect email.'});
                    }
                    // User exists but wrong password, log the error
                    user.comparePassword(password, function(err, isMatch){
                        if(isMatch){
                            return done(null,user);
                        }else{
                            return done(null,false, {message: 'Incorrect Password'});
                        }
                    });
                });
        }));
};