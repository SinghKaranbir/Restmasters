var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var User = mongoose.model('User');
var async = require('async');
var crypto = require('crypto');

module.exports = function(passport){

    // Login
    router.get('/login', function(req, res, next) {
        req.body = req.query;
        passport.authenticate('login', function(err, user, info) {

            if (err) { return next(err); }

            if (!user) { return res.status(404).send({message: info.message}); }

            req.logIn(user, function(err) {
                if (err) { return next(err); }
                return res.status(200).send({user: user});
            });
        })(req, res, next);
    });


    //Registration of User.
    router.post('/register', function(req, res) {
        
        User.findOne({ 'email' :  req.body.email }, function(err, user) {

            // In case of any error, return using the done method
            if (err){
                return res.status(500).send({state: 'failure',message: 'Internal Error'});
            }

            // already exists
            if (user) {
                console.log('User Already exists with email '+req.body.email);
                return res.status(422).send({ message: 'User Already exists with email '+req.body.email});
            }else {

                var newUser = new User();

                // set the user's local credentials
                        newUser.email = req.body.email;
                        newUser.password = req.body.password;
                        newUser.firstName = req.body.firstName;
                        newUser.lastName = req.body.lastName;
                        newUser.userType = req.body.userType;
                        newUser.save(function (err) {
                            if (err) console.log(err);
                        });

                return res.status(201).send();
                }
        });
    });

    //log out
    router.get('/signout', function(req, res) {
        req.logout();
        res.status(200).send({state: 'success', message: "Signed Out"});
    });

    router.put('/addToken', function(req,res){

        User.find({'email': req.body.email}, function (err, users) {
            if(err) console.log(err);

            console.log("length of users -->" + users.length);
            
            if(users.length == 0){
                    console.log("users not found");
                    return res.status(404).send({message: 'No users Listed'});
            }else{
                User.update({'email': req.body.email}, {$set : {regId: req.body.regId}}, {upsert:true}, function(err){
                if (err){
                    console.log(err);
                }else{
                    return res.status(204).send({message: 'Successfully Updated'})
                    }     
                });
            }      
        });
    });

   /* //Update the user values
    router.route('/update')

        .put(function(req,res){
            User.findById(req.user.id, function(err, user){
                if(!user){
                    return res.send({state: 'failure', user: null, message: "No User with that email" + req.body.email});
                }else{
                    user.password= req.body.password;
                    user.firstName = req.body.firstName;
                    user.lastName = req.body.lastName;
                    user.address = req.body.address;
                    user.address2 = req.body.address2;
                    user.city = req.body.city;
                    user.state = req.body.state;
                    user.phoneNumber = req.body.phoneNumber;

                    // save the user
                    user.save(function(err) {
                        if (err)
                            console.log(err);
                        return res.send({state: 'success', message: "Successfully Updated"})
                    });
                }
            });
        });*/
    return router;

};