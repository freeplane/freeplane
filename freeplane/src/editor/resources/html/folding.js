// Here we implement folding. It works fine with MSIE5.5, MSIE6.0 and
// Mozilla 0.9.6.

if (document.layers) {
    //Netscape 4 specific code
    pre = 'document.';
    post = ''; }
if (document.getElementById) {
    //Netscape 6 specific code
    pre = 'document.getElementById("';
    post = '").style'; }
if (document.all) {
    //IE4+ specific code
    pre = 'document.all.';
    post = '.style'; }

function layer_exists(layer) {
    try {
	eval(pre + layer + post);
	return true; }
    catch (error) {
	return false; }}

function show_layer(layer) {
    eval(pre + layer + post).position = 'relative';
    eval(pre + layer + post).visibility = 'visible'; }

function hide_layer(layer) {
    eval(pre + layer + post).visibility = 'hidden';
    eval(pre + layer + post).position = 'absolute'; }

function hide_folder(folder) {
    hide_folding_layer(folder);
    show_layer('show'+folder);

    scrollBy(0,0); // This is a work around to make it work in Browsers (Explorer, Mozilla)
}

function show_folder(folder) {
    // Precondition: all subfolders are folded

    show_layer('hide'+folder);
    hide_layer('show'+folder);
    show_layer('fold'+folder);

    scrollBy(0,0); // This is a work around to make it work in Browsers (Explorer, Mozilla)

    var i;
    for (i=1; layer_exists('fold'+folder+'_'+i); ++i) {
	show_layer('show'+folder+'_'+i); }
}
function show_folder_completely(folder) {
    // Precondition: all subfolders are folded

    show_layer('hide'+folder);
    hide_layer('show'+folder);
    show_layer('fold'+folder);

    scrollBy(0,0); // This is a work around to make it work in Browsers (Explorer, Mozilla)

    var i;
    for (i=1; layer_exists('fold'+folder+'_'+i); ++i) {
	show_folder_completely(folder+'_'+i); }
}



function hide_folding_layer(folder) {
    var i;
    for (i=1; layer_exists('fold'+folder+'_'+i); ++i) {
	hide_folding_layer(folder+'_'+i); }

    hide_layer('hide'+folder);
    hide_layer('show'+folder);
    hide_layer('fold'+folder);

    scrollBy(0,0); // This is a work around to make it work in Browsers (Explorer, Mozilla)
}

function fold_document() {
    var i;
    var folder = '1';
    for (i=1; layer_exists('fold'+folder+'_'+i); ++i) {
	hide_folder(folder+'_'+i); }
}

function unfold_document() {
    var i;
    var folder = '1';
    for (i=1; layer_exists('fold'+folder+'_'+i); ++i) {
	show_folder_completely(folder+'_'+i); }
}
