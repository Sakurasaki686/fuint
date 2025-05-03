<template>
  <div :id="id" :chartType="chartType" :headList="headList" :dataList="dataList" :style="{height:height, width:width}" />
</template>

<script>
const serieDataItem = {
    name: '',
    type: '',
    barMaxWidth: 50,
    barGap: '10%',
    itemStyle: {
      borderWidth: 1,
      borderColor: "#8fa1e6",
      areaColor: "#b0defa",
      normal: {
        color: '#59aff2',
        label: {
          show: true,
          textStyle: {
            color: '#666666'
          },
          position: 'top',
          formatter(p) {
            return p.value > 0 ? p.value : ''
          }
        }
      }
    },
    data: []
}
import * as echarts from 'echarts'
import resize from './mixins/resize'

export default {
  mixins: [resize],
  props: {
    id: {
      type: String,
      default: 'chart'
    },
    title: {
      type: String,
      default: ''
    },
    color: {
      type: String,
      default: '#EE9A49'
    },
    chartType: {
      type: String,
      default: 'bar'
    },
    headList: {
      type: [Array],
      default: () => []
    },
    dataList: {
      type: [Array],
      default: () => []
    },
    width: {
      type: String,
      default: '200px'
    },
    height: {
      type: String,
      default: '200px'
    }
  },
  data() {
    return {
      chart: null,
      yName: ''
    }
  },
  mounted() {
    this.initChart()
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  watch: {
    chartType() {
      this.initChart()
    },
    headList() {
      this.initChart()
    }
  },
  methods: {
    initChart() {
      const app = this
      this.chart = null
      this.chart = echarts.init(document.getElementById(this.id))
      const xData = []
      if (app.dataList && app.dataList.length > 0) {
          app.dataList.forEach(function(item) {
            xData.push(item.name)
          })
      }
      const series = []
      if (app.headList && app.headList.length > 0) {
          app.headList.forEach(function(header, index) {
              const serie = JSON.parse(JSON.stringify(serieDataItem))
              serie.name = header
              serie.type = app.chartType
              serie.itemStyle.normal.color = app.color
              serie.data = []
              const valueList = []
              app.dataList.forEach(function(value) {
                  valueList.push(value['value' + index])
              })
              serie.data = valueList
              series.push(serie)
          })
      }

      app.chart.setOption({
        backgroundColor: '#FFFFFF',
        title: {
          text: app.title,
          x: 'center',
          top: '20',
          textStyle: {
            color: '#333333',
            fontSize: '14'
          },
          subtextStyle: {
            color: '#666666',
            fontSize: '16'
          }
        },
        tooltip: {
          show: true,
          trigger: 'axis',
          axisPointer: {
            type: 'none',
            snap: false,
            label: {
              margin: 10
            },
            textStyle: {
              color: '#666666'
            }
          },
          triggerOn: 'mousemove',
          showContent: true,
          alwaysShowContent: false,
          borderWidth: 0,
          confine: false,
          formatter(p) {
            return p[0].name + ' : ' + p[0].value
          }
        },
        grid: {
          left: '5%',
          right: '5%',
          borderWidth: 0,
          top: 60,
          bottom: 50,
          textStyle: {
            color: '#666666'
          }
        },
        legend: {
          x: 'center',
          bottom: 'bottom',
          textStyle: {
            color: '#666666'
          },
          data: this.headList
        },
        calculable: true,
        xAxis: [{
          type: 'category',
          axisLine: {
            lineStyle: {
              color: '#666666'
            }
          },
          splitLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          splitArea: {
            show: false
          },
          axisLabel: {
            interval: 0
          },
          data: xData
        }],
        yAxis: [{
          type: 'value',
          name: app.yName,
          splitLine: {
            lineStyle: {
              type: 'dashed'
            },
            show: true
          },
          axisLine: {
            lineStyle: {
              color: '#666666'
            }
          },
          axisTick: {
            show: false
          },
          axisLabel: {
            interval: 0
          },
          splitArea: {
            show: false
          }
        }],
        series: series
      }, true)
    }
  }
}
</script>
