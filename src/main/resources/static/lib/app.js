var app = angular.module('app', []);
app.controller('appCtrl', function($scope, $http){

    $scope.getConfig = function(){
        $http.get('/serverConfig', function(res){
            return res.data;
        }).then(function(rs){
            $scope.config = rs.data;
        });
    };

    $scope.turnOn = function(){
        var config = $scope.config;
        $http.get('/turnOn', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.turnOff = function(){
        var config = $scope.config;
        $http.get('/turnOff', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.getTime = function(){
        var config = $scope.config;
        $http.get('/time', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.getBrightness = function(){
        var config = $scope.config;
        $http.get('/brightness', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.setBrightness = function(){
        var config = $scope.config;
        $http.get('/setBrightness', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.autoBrightness = function(){
        var config = $scope.config;
        $http.get('/autoBrightness', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    };

    $scope.screenShot = function(){
        var config = $scope.config;
        $http.get('/screenShot', {params: {config: config}}, function(res){
            return res.data;
        }).then(function(rs){
            return rs.data;
        });
    }
})