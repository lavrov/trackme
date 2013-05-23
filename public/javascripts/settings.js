$(function(){
    processForm({
        grantPermission: function(object){
          location.reload();
        }
    });
});

function processForm(successCallbacks) {
    $('form').each(function(){
        var action = this.action;
        var formName = this.name;
        var form = $(this);
        var submit = form.find('button[type=submit]');
        submit.click(function(){
            $.ajax({
                type : 'GET',
                url : action,
                data : form.serialize(),
                dataType : "text",
                success : successCallbacks[formName]
            });
            return false;
        });
    })
}