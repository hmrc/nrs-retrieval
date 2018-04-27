NrsAjax = {
    model: function() {
        var sampleInput = document.getElementById("element-id").value;
        var model = {};
        model.field1 = sampleInput;
        return model;
    },
    http: function() {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onload = function (e) {
            if (xmlhttp.readyState === 4 && xmlhttp.status === 200) {
                //TODO
            }
        };
        return xmlhttp;
    },
    download: function() {
        var model = this.model();
        var xmlhttp = this.http();
        xmlhttp.open("GET", '@controllers...SampleNrsControllerController.download(id).absoluteURL()');
        xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        xmlhttp.send(JSON.stringify(model));
    },
};