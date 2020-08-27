$(function(){
    // Checking for CSS 3D transformation support
    $.support.css3d = supportsCSS3D();

    var container = $('#flip-container');
    $('#toRecover').on('click', function(e){
        flipit($('#recover'), e);
    });
    $('#toSignup').on('click', function(e){
        flipit($('#signup'), e);
    });
    $('.backToSignin').on('click', function(e){
        flipit($(this).parent(), e);
    });

    formContainer.find('form').submit(function(e){
        // Preventing form submissions. If you implement
        // a backend, you might want to remove this code
        e.preventDefault();
    });
    function ctoggle(){
        return container.toggleClass('flipped').delay(200);
    }
    function flipit(el, e){
        $.when(ctoggle()).done(function(){
            el.toggleClass('hide');
            if(!$.support.css3d)
                $('#signin').toggle();
        });
        e.preventDefault();
    }
    // A helper function that checks for the
    // support of the 3D CSS3 transformations.
    function supportsCSS3D() {
        var props = [
            'perspectiveProperty', 'WebkitPerspective', 'MozPerspective'
        ], testDom = document.createElement('a');

        for(var i=0; i<props.length; i++){
            if(props[i] in testDom.style){
                return true;
            }
        }

        return false;
    }
});
