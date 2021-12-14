# 智能置物箱之設計與實作 (Design and Implementation of a Smart Deposit Box)
## 摘要
本論文設計與實作出一套智慧型置物箱系統，使用者能利用快速的 NFC 感應開鎖，解決傳統置物櫃的不方便與密碼遺忘等問題。實驗利用 Arduino 的模組化系統來設計智慧型置物箱的基礎架構，並透過編寫 Android 程
式取得智慧手機中的 IMEI 號碼創建使用者資料，再依據 IMEI 號碼能有效管控帳號的唯一性、可驗證性與安全性，並透過 NFC 傳送密碼解鎖模組來開發置物櫃系統。

## NFC 選擇依據 
### 無線通信技術的比較
選擇 NFC 主要是根據 NFC 的幾項特點 
1.短距離的通信 
2.耗電量低 
3.採一對一的方式 

基於以上原因能使裝置擁有較高的保密性與安全性。

<img src="https://github.com/AlexChang19/Design-and-Implementation-of-a-Smart-Deposit-Box/blob/c3cea1fa7bba339f60490200120c992cf777abc5/image/%E7%84%A1%E7%B7%9A%E9%80%9A%E8%A8%8A%E6%AF%94%E8%BC%83.jpg"><br/>

### NFC 手機市占率
關於NFC技術的必要性，在2012年Google Android 手機上開始全面支援NFC功能，2015年全球近場通信（NFC）市場規模估計48億美元。

<img src="https://github.com/AlexChang19/Design-and-Implementation-of-a-Smart-Deposit-Box/blob/9c38e8d117ff82add65a598993e4247e4a6e28f7/image/%E5%B8%82%E5%A0%B4%E8%AA%BF%E6%9F%A5.jpg"><br/>

## 雛型設計
使用手機感應置物櫃上的 NFC 感應區，便可觸動電磁閥做動，從而使置物櫃門自動打開。
<img src="https://github.com/AlexChang19/Design-and-Implementation-of-a-Smart-Deposit-Box/blob/b88169596e74f28d90fae41b5d6b03dff08f93bb/image/Design%20drawing.jpg" width="750" height="500" alt="Design drawing"/><br/>

## 開發環境使用
1. Android IDE
2. Arduino IDE

## 開發模組選擇
1.	Arduino mega2560 
2.	NFC Shield V2.0 智慧標籤讀卡器 
3.	LCD顯示器型 (1602)
4.	5V電磁閥 (DS-0420S)
5.	SD卡模組 Arduino Micro SD
6.	單路繼電器模組(光耦隔離)
7.	Real-Time Clock (ZS-042/DS3231)


## 分析比對傳統與智慧型置物櫃
傳統機械式鑰匙鎖的置物櫃屬於不記名使用，在丟失鑰匙的時候不僅想要取回物品有一定的困難度，
但NFC智能鎖不單使用上更便捷，無須攜帶鑰匙、不怕丟失、也不怕忘記密碼，更在實名制上做到了更大的物品保管安全性。
![image](https://user-images.githubusercontent.com/85589138/146061694-ee54977f-3ded-4e47-b4e9-662cc6f244b9.png)


## 結論

本次實驗Demo出的NFC置物櫃開鎖系統整，雖然整體美觀度是以較為簡陋大方的方式呈現，但系統操作起來還算是流暢度完整的。能夠實現本專題所要呈現出的效果。相信不遠的將來就能看到智能儲物櫃完全取代傳統儲物櫃，為人類生活帶來極大的便利性與安全性。


## 未來展望

未來對智能儲物櫃的布置也可以依照需求，打造出單機款式或是一對百的款式，更貼近生活需求，為生活創造更多便利才是本次實驗的核心目標。

![image](https://user-images.githubusercontent.com/85589138/146061979-c157d3c9-68ba-43fe-9ca6-cce808dccf80.png)
![image](https://user-images.githubusercontent.com/85589138/146062290-7036b726-46bd-46c1-8fd7-198af885f00a.png)


