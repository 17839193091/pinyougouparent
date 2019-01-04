<html>
    <head>
        <title>demo</title>
        <meta charset="utf-8"/>
    </head>
    <#--<body>
        <#include "head.ftl">
        &lt;#&ndash;注释&ndash;&gt;
        ${name},你好.${message}<br>

    <#assign linkman="周先生">
    ${linkman}
    <#if success=true>
        你已通过实名认证
    <#else>
        未通过实名认证
    </#if>-->
    <body>
        -----商品列表-------<br>
        <#list goodsList as goods>
            ${goods_index + 1} ${goods.name}   ${goods.price}<br>
        </#list>
        一共${goodsList ? size}条记录<br>

        <#assign text="{'bank':'工商银行','account':'111122225542211333'}">
        <#assign data = text ? eval>
        开户行:${data.bank}<br>
        账户:${data.account}<br>
        ------------------------------<br>
        当前日期:${today ? date} <br>
        当前时间:${today ? time} <br>
        当前日期时间:${today ? datetime} <br>

        日期格式化:${today ? string("yyyy-MM-dd HH:mm:ss SSS")} <br>

        当前积分:${point ? c} <br>

        <#if aaa??>
            aaa存在
            <#else >
            aaa不存在
        </#if>

        ${bbb!''}

    </body>
</html>