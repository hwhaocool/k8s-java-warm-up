
function getQueryString(name) {
    var result = window.location.search.match(new RegExp("[\?\&]" + name + "=([^\&]+)", "i"));
    if (result == null || result.length < 1) {
        return "";
    }
    return result[1];
}

String.prototype.template = function() {
    var args = arguments;                
    return this.replace(/\{(\d+)\}/g, function(m,i)
                                        {
                                            return args[i];
                                        }
                         );
};

function getAndSetEcharts(id, type, requestId) {
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById(id));
    
    myChart.showLoading();    //数据加载完之前先显示一段简单的loading动画
    
    var url = "/api/warmup/monitor/info?id=" + requestId;
    console.log(url);

    toastr.options = {
      "positionClass": "toast-top-center",
    }

    $.ajax({
        type : "get",
        async : true,            //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
        url : url,
        data : {},
        dataType : "json",        //返回数据形式为json
        success : function(result) {
            //请求成功时执行该函数内容，result即为服务器返回的json对象
            if (result) {
                console.log(result);
                
                myChart.hideLoading();
                myChart.setOption(result);

                toastr.success('加载成功');
            }
       },
       error : function(errorMsg) {
           //请求失败时执行该函数
           toastr.success('图表请求数据失败!');
           myChart.hideLoading();
       }
    });
}


