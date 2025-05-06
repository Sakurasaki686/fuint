<template>
  <view class="container">
    <!-- 聊天消息列表区域 -->
    <scroll-view
      class="chat-container"
      scroll-y="true"
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
      @scrolltoupper="loadHistory"
    >
      <view class="chat-list">
        <!-- 欢迎消息 - 始终显示 -->
        <view class="chat-item ai">
          <view class="avatar">AI</view>
          <view class="message">
            <text>你好，我是 Muse，你的智能助手。有什么我可以帮助你的吗？</text>
          </view>
        </view>

        <!-- 消息列表 -->
        <view
          v-for="(item, index) in messageList"
          :key="item.time || index"
          :class="['chat-item', item.isUser ? 'user' : 'ai']"
        >
          <view class="avatar">{{ item.isUser ? '我' : 'AI' }}</view>

          <!-- **** 根据消息类型渲染不同的内容 **** -->
          <view class="message" :class="{ 'error-message': item.isError }">

            <!-- 1. 如果是商品推荐 -->
            <view v-if="item.isRecommendation">
              <text class="recommendation-text">{{ item.recommendationText }}</text>
              <view class="product-list">
                <view
                  class="product-item"
                  v-for="(product, pIndex) in item.recommendedProducts"
                  :key="product.id || pIndex"
                  @click="goToProductDetail(index, pIndex)"  
                  
                >
                  <image class="product-image" :src="product.imageUrl" mode="aspectFill"></image>
                  <text class="product-name">{{ product.name }}</text>
                </view>
              </view>
            </view>

            <!-- 2. 如果是普通文本消息 -->
            <view v-else>
              <!-- 思考内容区域 -->
              <view v-if="item.thinkContent && !item.isError" class="think-content">
                <view class="think-header">
                  <text class="think-icon">💭</text>
                  <text class="think-title">思考过程</text>
                  <text class="think-toggle" @click="toggleThink(index)">{{ item.showThink ? '收起' : '展开' }}</text>
                </view>
                <view v-if="item.showThink" class="think-body">
                  <text>{{ item.thinkContent }}</text>
                </view>
              </view>
              <!-- 内容显示区域 -->
              <text>{{ item.isUser ? item.content : item.displayContent }}</text>
              <!-- 打字光标 -->
              <view class="typing-cursor" v-if="!item.isUser && item.isTyping && !item.isError"></view>
            </view>

          </view> <!-- message 结束 -->
        </view> <!-- chat-item 结束 -->

        <!-- 加载中提示 -->
        <view class="chat-item ai" v-if="loading">
           <view class="avatar">AI</view>
           <view class="message loading">
             <view class="loading-dots">
               <view class="dot"></view><view class="dot"></view><view class="dot"></view>
             </view>
           </view>
        </view>
      </view>
    </scroll-view>

    <!-- 模型选择区域 -->
    <view class="model-selector">
       <view class="selector-label">选择模型：</view>
       <view class="selector-box">
         <picker @change="onModelChange" :value="selectedModelIndex" :range="modelOptions">
           <view class="picker-value">
             {{ modelOptions[selectedModelIndex] }}
             <text class="iconfont icon-xiangxiajiantou"></text>
           </view>
         </picker>
       </view>
       <view class="clear-btn" @click="clearChat">清空对话</view>
    </view>

    <!-- 底部输入区域 -->
    <view class="input-container">
       <input
         class="input-box"
         type="text"
         v-model="inputMessage"
         placeholder="请输入您的问题..."
         confirm-type="send"
         @confirm="sendMessage"
         :adjust-position="false"
       />
       <view class="send-btn" @click="sendMessage">发送</view>
    </view>
  </view>
</template>

<script>
  // 如果需要解析链接，可以保留 parseLinks 函数
  // const URL_REGEX = /((?:https?:\/\/|www\.)[^\s<>"{}|\\^`[\]]+)/g;

  export default {
    data() {
      return {
        messageList: [],
        chatHistory: [],
        inputMessage: '',
        loading: false,
        scrollTop: 0,
        typingSpeed: 30,
        currentTypingIndex: -1,
        typingTimer: null,
        modelOptions: ['qwen2.5:1.5b', 'deepseek-r1:1.5b', 'gemma3:1b', 'mistral:7b'],
        selectedModelIndex: 0
      }
    },
    onLoad() {
    },
    methods: {

      toggleThink(index) {
        const item = this.messageList[index];
        if (item) {
            this.$set(item, 'showThink', !item.showThink);
        }
      },

      onModelChange(e) {
        this.selectedModelIndex = e.detail.value;
      },

      clearChat() {
        uni.showModal({
          title: '确认清空',
          content: '确定要清空当前所有对话吗？',
          success: (res) => {
            if (res.confirm) {
              this.messageList = [];
              this.chatHistory = []; // 清空历史记录
              this.inputMessage = '';
              if (this.typingTimer) {
                clearInterval(this.typingTimer);
                this.typingTimer = null;
              }
              this.currentTypingIndex = -1;
              uni.showToast({ title: '对话已清空', icon: 'success', duration: 1500 });
              this.scrollToBottom(); // 清空后也滚动一下，确保视口正确
            }
          }
        });
      },

      sendMessage() {
        const trimmedMessage = this.inputMessage.trim();
        if (!trimmedMessage) return;

        const userMessage = trimmedMessage;

        // 停止当前可能正在进行的打字效果
        if (this.typingTimer) {
            clearInterval(this.typingTimer);
            // 确保最后一条AI消息的isTyping状态被设置为false
            if(this.currentTypingIndex >= 0 && this.currentTypingIndex < this.messageList.length) {
                this.$set(this.messageList[this.currentTypingIndex], 'isTyping', false);
            }
            this.typingTimer = null;
            this.currentTypingIndex = -1;
        }


        // 添加用户消息
        this.messageList.push({
          content: userMessage,
          displayContent: userMessage, // 用户消息直接完整显示
          isUser: true,
          isError: false,
          time: new Date().getTime() // 添加时间戳作为key的备选
        });
        // 将用户消息加入历史
        this.chatHistory.push({ role: "user", content: userMessage });

        this.inputMessage = '';
        this.scrollToBottom();
        this.loading = true;

        // 调用后端 API
        uni.request({
          url: 'http://localhost:8080/ollama/generate', // 确认后端地址
          method: 'POST',
          data: {
            prompt: userMessage,
            model: this.modelOptions[this.selectedModelIndex],
            history: this.chatHistory // 发送包含当前用户消息的完整历史
          },
          header: { 'content-type': 'application/json' },
          success: (res) => {
            this.loading = false;
            let messageIndex = -1;

            if (res.statusCode === 200 && res.data) {
              if (res.data.type === 'recommendation') {
                // --- 处理商品推荐响应 ---
                console.log("收到商品推荐响应");
                this.messageList.push({
                  isUser: false,
                  isRecommendation: true,
                  recommendationText: res.data.message || "为您找到以下商品：",
                  recommendedProducts: res.data.products || [],
                  isError: false,
                  time: new Date().getTime()
                });
                // 推荐结果不加入 chatHistory

              } else if (res.data.type === 'text' && typeof res.data.response === 'string') {
                 // --- 处理普通文本响应 ---
                 const responseText = res.data.response;
                 let thinkContent = null;
                 let displayContent = responseText;

                 const thinkMatch = displayContent.match(/<think>([\s\S]*?)<\/think>/);
                 if (thinkMatch) {
                   thinkContent = thinkMatch[1].trim();
                   displayContent = displayContent.replace(/<think>[\s\S]*?<\/think>/, '').trim();
                 }

                 // 添加 AI 回复消息对象
                 messageIndex = this.messageList.length;
                 const isErrorMessage = responseText.startsWith("抱歉") || responseText.startsWith("请求处理失败") || responseText.startsWith("与AI服务通信时") || responseText.startsWith("AI 服务返回错误");
                 this.messageList.push({
                   content: displayContent,
                   displayContent: '',
                   thinkContent: thinkContent,
                   showThink: false,
                   isUser: false,
                   isTyping: true,
                   isError: isErrorMessage,
                   time: new Date().getTime()
                 });

                 // 将 AI 的有效回复（非错误）加入历史
                 if (!isErrorMessage) {
                    this.chatHistory.push({ role: "assistant", content: displayContent });
                 }

                 // 开始打字机效果
                 this.startTypewriterEffect(messageIndex);

              } else {
                  // 未知响应类型或数据格式错误
                  console.error("收到未知格式的后端响应:", res.data);
                  this.showError("抱歉，系统返回的数据格式不正确。");
              }
            } else {
               // HTTP 状态码错误或其他网络问题
               console.error("API 请求失败:", res);
               const errorMsg = res.data?.response || res.data?.error || `请求失败，状态码: ${res.statusCode || '未知'}`;
               this.showError(errorMsg);
            }
            this.scrollToBottom();
          },
          fail: (err) => {
             this.loading = false;
             console.error('请求失败:', err);
             this.showError('网络连接错误或服务无响应，请稍后再试。');
             this.scrollToBottom();
          }
        });
      },

      // 显示错误消息的辅助函数
      showError(errorMessage) {
         // 停止当前可能在打字的计时器
         if (this.typingTimer) {
             clearInterval(this.typingTimer);
             if(this.currentTypingIndex >= 0 && this.currentTypingIndex < this.messageList.length) {
                 this.$set(this.messageList[this.currentTypingIndex], 'isTyping', false);
             }
             this.typingTimer = null;
             this.currentTypingIndex = -1;
         }

         const messageIndex = this.messageList.length;
         this.messageList.push({
           content: errorMessage,
           displayContent: '', // 错误也用打字效果
           isUser: false,
           isTyping: true,
           isError: true, // 标记为错误
           time: new Date().getTime()
         });
         this.startTypewriterEffect(messageIndex);
      },

      // 跳转到商品详情页
      goToProductDetail(messageIndex, productIndex) { // 接收两个索引
        try { // 最好加上 try-catch 增加健壮性
          // 通过索引从 data 中的 messageList 查找数据
          const messageItem = this.messageList[messageIndex];
          if (!messageItem || !messageItem.recommendedProducts) {
              console.error("无法找到对应的消息或商品列表", messageIndex);
              uni.showToast({ title: '查找商品信息出错', icon: 'none' });
              return;
          }

          const product = messageItem.recommendedProducts[productIndex];
          if (!product) {
              console.error("无法找到对应的商品", messageIndex, productIndex);
              uni.showToast({ title: '查找商品信息出错', icon: 'none' });
              return;
          }

          const productId = product.id; // 从找到的 product 对象中获取 id

          console.log("goToProductDetail called with indices. MessageIndex:", messageIndex, "ProductIndex:", productIndex);
          console.log("Found Product:", product);
          console.log("Extracted productId:", productId);


          // --- 原有的 ID 检查和跳转逻辑 ---
          if (productId === null || typeof productId === 'undefined' || productId === '') {
              console.warn("无效的商品 ID (null, undefined, or empty string):", productId);
              uni.showToast({ title: '无法打开商品', icon: 'none' });
              return;
          }

          console.log("准备跳转到商品详情页, ID:", productId);
          const detailPageUrl = '/pages/goods/detail?goodsId=' + productId;
          uni.navigateTo({
            url: detailPageUrl,
            fail: (err) => {
                console.error("跳转失败:", err);
                uni.showToast({ title: '无法打开详情页面', icon: 'none' });
            }
          });

        } catch (error) {
            console.error("goToProductDetail 处理出错:", error);
            uni.showToast({ title: '处理点击时出错', icon: 'none' });
        }
      },

      // 开始打字机效果
      startTypewriterEffect(messageIndex) {
        if (this.typingTimer) { clearInterval(this.typingTimer); }
        this.currentTypingIndex = messageIndex;
        let charIndex = 0;

        // 健壮性检查
        if (messageIndex < 0 || messageIndex >= this.messageList.length) {
            console.error("无效的消息索引:", messageIndex);
            return;
        }
        const message = this.messageList[messageIndex];
        if (!message || typeof message.content !== 'string') {
            console.error("无效的消息对象或内容:", message);
             if(message) this.$set(message, 'isTyping', false);
            return;
        }
        // 如果内容为空，直接结束打字
        if (message.content.length === 0) {
            this.$set(message, 'isTyping', false);
            return;
        }

        const fullText = message.content;
        this.$set(message, 'isTyping', true); // 确保设置 isTyping
        this.$set(message, 'displayContent', ''); // 重置显示内容


        this.typingTimer = setInterval(() => {
          if (charIndex < fullText.length) {
            // 使用 $set 确保响应式更新
            this.$set(message, 'displayContent', fullText.substring(0, charIndex + 1));
            charIndex++;
            this.$nextTick(() => { this.scrollTop = 999999; });
          } else {
            clearInterval(this.typingTimer);
            this.typingTimer = null;
            this.$set(message, 'isTyping', false); // 确保 isTyping 被设置为 false
            this.currentTypingIndex = -1; // 重置当前打字索引
            this.$nextTick(() => { this.scrollTop = 999999; });
          }
        }, this.typingSpeed);
      },

      scrollToBottom() {
        // 稍微延迟滚动，给 DOM 更新一点时间
        setTimeout(() => {
            this.$nextTick(() => {
              this.scrollTop = 999999; // 设置一个足够大的值
            });
        }, 50); // 50毫秒延迟
      },

      loadHistory() {
        // 实际应用中可能需要实现分页加载历史记录的逻辑
        console.log('触发加载更多历史消息');
        // uni.showToast({ title: '没有更多历史消息了', icon: 'none' });
      }
    }
  }
</script>

<style lang="scss" scoped>
  /* 保持你之前的全部样式 */
  .container { display: flex; flex-direction: column; height: 100vh; background-color: #f5f5f5; }
  .chat-container { flex: 1; padding: 20rpx; box-sizing: border-box; }
  .chat-list { padding-bottom: 20rpx; }
  .chat-item { display: flex; margin-bottom: 30rpx; &.user { flex-direction: row-reverse; .avatar { background-color: #007AFF; } .message { background-color: #007AFF; color: #fff; margin-right: 20rpx; margin-left: 0; border-radius: 20rpx 4rpx 20rpx 20rpx; } } &.ai { .avatar { background-color: #00acac; } .message { background-color: #fff; margin-left: 20rpx; border-radius: 4rpx 20rpx 20rpx 20rpx; } } }
  .avatar { width: 80rpx; height: 80rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 28rpx; flex-shrink: 0; }
  .message { max-width: 80%; padding: 20rpx; font-size: 28rpx; word-break: break-all; box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1); position: relative; &.loading { padding: 20rpx 40rpx; } }
  .typing-cursor { display: inline-block; width: 2px; height: 20rpx; background-color: #333; margin-left: 4rpx; vertical-align: middle; animation: blink 0.7s infinite; }
  @keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
  .loading-dots { display: flex; align-items: center; .dot { width: 12rpx; height: 12rpx; border-radius: 50%; background-color: #999; margin: 0 6rpx; animation: dotAnimation 1.4s infinite ease-in-out; &:nth-child(1) { animation-delay: 0s; } &:nth-child(2) { animation-delay: 0.2s; } &:nth-child(3) { animation-delay: 0.4s; } } }
  @keyframes dotAnimation { 0%, 80%, 100% { transform: scale(0.6); opacity: 0.6; } 40% { transform: scale(1); opacity: 1; } }
  .input-container { display: flex; padding: 20rpx; background-color: #fff; border-top: 1rpx solid #eee; }
  .input-box { flex: 1; height: 80rpx; background-color: #f5f5f5; border-radius: 40rpx; padding: 0 30rpx; font-size: 28rpx; }
  .send-btn { width: 120rpx; height: 80rpx; background-color: #00acac; color: #fff; border-radius: 40rpx; display: flex; align-items: center; justify-content: center; margin-left: 20rpx; font-size: 28rpx; }
  .model-selector { display: flex; align-items: center; padding: 10rpx 20rpx; background-color: #fff; border-top: 1rpx solid #eee; }
  .selector-label { font-size: 28rpx; color: #333; margin-right: 20rpx; white-space: nowrap; }
  .selector-box { flex: 1; }
  .picker-value { font-size: 28rpx; color: #333; background-color: #f5f5f5; padding: 10rpx 20rpx; border-radius: 8rpx; display: flex; align-items: center; justify-content: space-between; .iconfont { font-size: 24rpx; color: #999; } } /* 添加箭头样式 */
  .think-content { margin-bottom: 10rpx; background-color: #f0f8ff; border-radius: 8rpx; overflow: hidden; }
  .think-header { display: flex; align-items: center; padding: 10rpx 16rpx; background-color: #e6f0fa; border-bottom: 1rpx solid #d0e3f2; }
  .think-icon { margin-right: 8rpx; font-size: 24rpx; }
  .think-title { flex: 1; font-size: 24rpx; color: #4a6fa5; font-weight: bold; }
  .think-toggle { font-size: 24rpx; color: #4a90e2; padding: 4rpx 10rpx; cursor: pointer; }
  .think-body { padding: 16rpx; font-size: 26rpx; color: #666; line-height: 1.5; max-height: 300rpx; overflow-y: auto; white-space: pre-wrap; } /* 添加 pre-wrap 保留换行和空格 */
  .clear-btn { padding: 10rpx 20rpx; background-color: #00acac; color: #fff; font-size: 26rpx; border-radius: 40rpx; margin-left: 20rpx; border: none; &:active { background-color: #009090; } }

  /* **** 新增推荐商品样式 **** */
  .recommendation-text {
    display: block;
    margin-bottom: 15rpx;
    color: #333;
  }

  .product-list {
    display: flex;
    flex-wrap: wrap;
    gap: 15rpx;
    margin-top: 10rpx;
  }

  .product-item {
    cursor: pointer; /* 在 H5 端会显示小手 */
    border: 1rpx solid #eee;
    border-radius: 10rpx;
    padding: 10rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: #ffffff;
    flex-basis: calc(33.333% - 10rpx); /* 一行最多3个 */
    min-width: 150rpx;
    box-sizing: border-box;
    transition: background-color 0.2s ease, transform 0.1s ease;

    &:active {
       background-color: #f0f0f0; /* 点击反馈 */
       transform: scale(0.98);
    }
  }

  .product-image {
    width: 120rpx;
    height: 120rpx;
    margin-bottom: 10rpx;
    border-radius: 8rpx;
    background-color: #f8f8f8; /* 图片加载时背景色 */
  }

  .product-name {
    font-size: 24rpx;
    color: #333;
    text-align: center;
    line-height: 1.3;
    width: 100%;
    /* 最多显示两行 */
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  /* 错误消息样式 */
  .error-message {
      background-color: #ffebee !important; /* 浅红色背景 */
      color: #c62828 !important; /* 深红色文字 */
  }
  .error-message .recommendation-text, /* 错误时不显示推荐文本 */
  .error-message .product-list { /* 错误时不显示商品列表 */
      display: none;
  }
  .error-message .think-content { /* 错误时不显示思考过程 */
      display: none;
  }

</style>