obj=document.getElementsByTagName('object');
for (var i=0; i<obj.length; ++i)
  obj[i].outerHTML=obj[i].outerHTML;
