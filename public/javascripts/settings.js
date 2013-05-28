$(function(){
    processForm({
        grantPermission: function(object){
          location.reload();
        },
        addNotification: function(responseBody) {
            console.log(responseBody);
        }
    });
});

function processForm(successCallbacks) {
    $('form').each(function(){
        var form = $(this);
        var formName = form.attr('name');
        var successCallback = successCallbacks[formName] ? successCallbacks[formName] : alert('Form submission callback undefined: ' + formName);
        var submit = form.find('button[type=submit]');
        submit.click(function(){
            $.ajax({
                type : form.attr('method'),
                url : form.attr('action'),
                data : form.serialize(),
                dataType : "text",
                success : successCallback
            });
            return false;
        });
    })
}