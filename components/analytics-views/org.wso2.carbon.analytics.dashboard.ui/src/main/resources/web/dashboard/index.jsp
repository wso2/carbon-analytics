<script type="text/javascript">
    jQuery(document).ready(function(){
        if (!window.location.origin) {
            window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
        }
        var url = window.location.origin + "/portal";
        var win = window.open(url, '_blank');
        win.focus();
    });
</script>
