﻿var wsDialogHtml=function(){this.updateCulturesSelect=function(b,d){for(var c=document.getElementById(b._.inputId),a=c.options.length-1;0<=a;a--)c.remove(a);for(a=0;a<d.length;a++)c.options.add(new Option(d[a],d[a]))}};
CKEDITOR.dialog.add("webspeechDialog",function(b){var d=new wsDialogHtml,c=b.ckWebSpeech._currentCulture.val;return{title:"WebSpeech Settings",minWidth:400,minHeight:200,contents:[{id:"tab-basic",label:"Basic Settings",elements:[{type:"select",id:"wslanguages",label:"Languages",items:b.ckWebSpeech.getLanguages(),"default":b.ckWebSpeech._currentCulture.langVal,onChange:function(a){var c=CKEDITOR.dialog.getCurrent().getContentElement("tab-basic","wscultures"),a=b.ckWebSpeech.getCultures(a.data.value);
c.setup({selCultures:c,options:a});c.fire("change",{value:a[0][0]},b)},onShow:function(){var a=CKEDITOR.dialog.getCurrent().getContentElement("tab-basic","wslanguages");document.getElementById(a._.inputId).value=b.ckWebSpeech._currentCulture.langVal}},{type:"select",id:"wscultures",label:"Culture",items:b.ckWebSpeech.getCultures(),"default":b.ckWebSpeech._currentCulture.val,onChange:function(a){c=a.data.value},setup:function(a){d.updateCulturesSelect(a.selCultures,a.options)},onShow:function(){var a=
CKEDITOR.dialog.getCurrent().getContentElement("tab-basic","wscultures");document.getElementById(a._.inputId).value=b.ckWebSpeech._currentCulture.val}}]},{id:"tab-adv",label:"Advanced Settings",elements:[]}],onOk:function(){b.ckWebSpeech.setDialectByCulture(c)}}});