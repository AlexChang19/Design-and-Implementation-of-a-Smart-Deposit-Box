#include <Wire.h> 
#include <SPI.h> 
#include <SD.h> 
#include <snep.h>
#include <PN532_SPI.h>
#include <NdefMessage.h>
#include <RTClib.h>
#include <LiquidCrystal_I2C.h>
LiquidCrystal_I2C lcd(0x3F,2,1,0,4,5,6,7,3,POSITIVE); //Lcd 3F(1602) 27(2004)
const int mSwitch = 2 , SD_CS=53 , NFC_CS = 9; // 開關輸出Pin 開關訊號 2pin sd選擇53pin nfc選擇9pin 
bool systemstatus = true ; //硬體檢查結果
bool cardstatus = false ,newuserpsw = false,removeuser = false , nfcdata=false; // 判斷判別狀況 
bool sd_user_login_data = true ; // 有無使用者資料
bool Leasestatus = false ; //租借狀態
int userquantity = 0 ; // 紀錄使用者數量
int delete_line = 0 ;  // 刪除行數
int Ndef_TNF_Type= 0 ; // TNF_Type 
String  payloadAsString =""; // NDEF檔案資料
String str_ndef_user_data[5],Str_SDUser[4],Str_sd_user_data[9],Lease_user_data[3],DataNC="0",times="0",ledshow="",buffer; //儲存使用者資料變數   
RTC_DS3231 rtc; // RTC 
Sd2Card card; SdVolume volume; SdFile root; File myFile; // SD 
PN532_SPI pn532spi(SPI, 9); SNEP nfc(pn532spi); uint8_t ndefBuf[128]; //NFC

void setup() {
     Serial.begin(9600);
     lcd.begin(16,2);
     lcd.cursor(); 
     lcd.blink(); 
     pinMode(mSwitch,OUTPUT); 
     pinMode(SD_CS,OUTPUT); 
     pinMode(NFC_CS,OUTPUT);
     digitalWrite(mSwitch,HIGH); 
     digitalWrite(SD_CS,HIGH);
     digitalWrite(NFC_CS,HIGH);
     rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));
     Serial.println("-----------System Boot Strap-----------");
     Systembootstrap();
     Serial.println("-Sdcard User ReadData and String Split-");
    sdcard_user_readData();
    Lease_Status();
}

void loop() {
 if(systemstatus == true){
      Serial.println("---------------NFC READ----------------");
      readnfc();
      delay(2000); 
      if(nfcdata == true){
      ndef_str_split(payloadAsString);
    if(Leasestatus == true){
    Lease_selectfunction();
    }else{
    selectfunction();
    }
      Result_judgment();
      delay(5000); 
      }
 }else {
    Serial.println("System error...");
    lcdprint("System error...");
    delay(5000); 
  }
}
// NFC Data Read
void readnfc(){
   nfcdata = false ; 
   digitalWrite(SD_CS,HIGH);
   digitalWrite(NFC_CS,LOW);
      if(Leasestatus== true){
    ledshow = "The box was rented by "+Lease_user_data[0];
    Serial.println(ledshow);
    lcdprint(ledshow);
   }else{
    Serial.println("Waiting for User request... ");
    lcdprint("Waiting for User request... ");
   }
   int MessageSize = nfc.read(ndefBuf,sizeof(ndefBuf));  
   if( MessageSize > 0 ){  
      NdefMessage Message  = NdefMessage(ndefBuf, MessageSize);  
      if( Message.getRecordCount() > 0 ){  
         NdefRecord record = Message.getRecord(0);  
            Serial.print("------ NDEF Message Data-----");
            Message.print(); //倒出整個封包  
        if (record.getTnf() == TNF_MIME_MEDIA ) {  
           if (record.getType() == "user_login" ||record.getType() == "imei"   ||record.getType() == "create_new_user"||record.getType() == "remove_user"){
              if(record.getType() == "user_login"){
                 Ndef_TNF_Type=1;
              }else if(record.getType() == "imei"){
                 Ndef_TNF_Type=2;
              }else if(record.getType() == "create_new_user"){
                 Ndef_TNF_Type=3;
              }else if(record.getType() == "remove_user"){
                 Ndef_TNF_Type=4;
              }
              int payloadLength = record.getPayloadLength();  
              byte payload[payloadLength];  
              record.getPayload(payload);
              payloadAsString = "" ;    
              for (int c = 0; c < payloadLength; c++) {  
          payloadAsString += (char)payload[c];   }
          Serial.println("----------Need NDEF Message------------");  
                    Serial.print(" Ndef_TNF_Type : "); Serial.println(Ndef_TNF_Type);
                    Serial.print(" PayloadAsString : "); Serial.println(payloadAsString);
                    nfcdata = true ; 
          return;
               }  
           }
       }
  }
  digitalWrite(NFC_CS,HIGH);
  Serial.println(" Request Again !!");
  lcdprint(" Request Again !!");
  delay(500);
  readnfc();
}

// 硬體檢測 
void Systembootstrap(){ 
 digitalWrite(NFC_CS,HIGH);
 digitalWrite(SD_CS,LOW);
 Serial.print("SD....");
if(!SD.begin(53)){
    Serial.println("Fail not find sd...");
    Serial.print("CS PIN");  Serial.println(SS_PIN);
    Serial.print("MOSI PIN");Serial.println(MOSI_PIN);
    Serial.print("MISO PIN");Serial.println(MISO_PIN);
    Serial.print("SCK PIN"); Serial.println(SCK_PIN);  
    systemstatus = false;  
    return ;  
}else
        if(!card.init(SPI_HALF_SPEED, 53)){  
           Serial.print("Fail not find sd...");  
           systemstatus = false; 
           return ;   
        }else
            if(!SD.exists("UserData.txt")){
        Serial.println("Fail not find UserData.txt...");
                sd_user_login_data = false ; 
                myFile = SD.open("UserData.txt",FILE_WRITE);  
                myFile.close(); 
            }else
            if(!SD.exists("LEASE.txt")){
        Serial.println("Fail not find LEASE.txt..."); 
                myFile = SD.open("LEASE.txt",FILE_WRITE);  
                myFile.close(); 
            }             
                digitalWrite(SD_CS,HIGH);
                digitalWrite(NFC_CS,LOW);
                Serial.println("ok !");
                Serial.print("RTC...");
                if(!rtc.begin()){
          Serial.println("Fail not find RTC");  
          systemstatus = false ; 
          return ; 
                } 
                    Serial.println("ok !");
                    systemstatus = true; 
                    delay(500);
                    return ;
}
//確認租借狀態與帳號倒出
void Lease_Status(){
  int commaPosition = 0,z=-1;
  String lease_user,lease_user_data;
    for(int z1=0;z1<=3;z1++){
    Lease_user_data[z1]="";}
  digitalWrite(SD_CS,LOW);
  digitalWrite(NFC_CS,HIGH);
  myFile = SD.open("LEASE.txt");
  Leasestatus = false ; 
  while (myFile.available()){
    buffer = myFile.readStringUntil('\n');
    buffer.trim();
    if(buffer.length()>0){
      lease_user = buffer ;
    }
      buffer.remove(0);  
      delay(100);   
  }
    myFile.close();  
    digitalWrite(SD_CS,HIGH);
    digitalWrite(NFC_CS,LOW);
    if(lease_user.length() > 0){
      Leasestatus = true ;
      do{   
         z=z+1;
               commaPosition = lease_user.indexOf(',');
               Serial.print(" commaPosition : "); Serial.println(commaPosition);    
               if(commaPosition != -1){    
                 lease_user_data = lease_user.substring(0,commaPosition);  
                 Lease_user_data[z]=lease_user_data;  
                 lease_user = lease_user.substring(commaPosition+1,lease_user.length());  
                 }else{    
                      if(lease_user.length()>0){
                        lease_user_data = lease_user;
                        Lease_user_data[z]= lease_user_data;}    
                     }                 
            }  while(commaPosition >= 0) ;  
  }else{
    Serial.println("------------Not User Lease --------------");
    Leasestatus ==false ; 
    return;
  }
Serial.println("------------Lease_Status--------------");
Serial.print(" User  Name : "); Serial.println(Lease_user_data[0]);
Serial.print(" User  Password : "); Serial.println(Lease_user_data[1]);
Serial.print(" User  IMEI : "); Serial.println(Lease_user_data[2]);
}
//解鎖
void Lease_selectfunction(){
      cardstatus = false ;
      char select_leasename[20];char select_leasepswd[20]; char select_leaseimei[20];char select_ndefname[20]; char select_ndefpwsd[20];
        Serial.println("Do the Lease User ");
        Lease_user_data[0].toCharArray(select_leasename,Lease_user_data[0].length()+1);
        Lease_user_data[1].toCharArray(select_leasepswd,Lease_user_data[1].length()+1);
        str_ndef_user_data[0].toCharArray(select_ndefname,str_ndef_user_data[0].length()+1);
        str_ndef_user_data[1].toCharArray(select_ndefpwsd,str_ndef_user_data[1].length()+1);
      if(Ndef_TNF_Type == 1){
      if(strcmp(select_leasename,select_ndefname) == 0 && strcmp(select_leasepswd,select_ndefpwsd) == 0 ){
        Serial.println("User  unlock ");
        lcdprint("User  unlock ");
        str_ndef_user_data[2] = Lease_user_data[2];
        cardstatus = true ;
        return ; 
      } } else if (Ndef_TNF_Type == 2){
             if(str_ndef_user_data[2].toInt()==Lease_user_data[2].toInt()){
              Serial.println("User  unlock ");
              lcdprint("User  unlock ");
              str_ndef_user_data[0] = Lease_user_data[0];
              str_ndef_user_data[1] = Lease_user_data[1];
              cardstatus = true ;
              return ;
              }}else cardstatus = false ;
               Serial.println("UserName or Password  error .. ");
              lcdprint("UserName or Password error .. ");
}
// 依選項比對字
void selectfunction(){
    Serial.println("------------Select Function------------");
    int commaPosition = 0 ,i = 0 ;
    char select_sdname[20]; char select_sdpswd[20]; char select_sdimei[20]; char select_ndefname[20]; char select_ndefpwsd[20]; char select_ndefimei[20]; char select_ndefadmin[20]; char select_sdAdmin[20];
    cardstatus = false ; newuserpsw = false ; removeuser = false ;
    switch(Ndef_TNF_Type){
    case 1 :
          Serial.println("Do the Case 1 User Login ");
          if(sd_user_login_data == true){
           if(userquantity>0){
                Str_sd_user_data[0].toCharArray(select_sdname,Str_sd_user_data[0].length()+1);
                Str_sd_user_data[1].toCharArray(select_sdpswd,Str_sd_user_data[1].length()+1);
                str_ndef_user_data[0].toCharArray(select_ndefname,str_ndef_user_data[0].length()+1);
                str_ndef_user_data[1].toCharArray(select_ndefpwsd,str_ndef_user_data[1].length()+1);
      if(strcmp(select_sdname,select_ndefname) == 0 && strcmp(select_sdpswd,select_ndefpwsd) == 0 ){
        Serial.println("User 1 Login ");
        lcdprint("User 1 Login ");
        cardstatus = true ;
        str_ndef_user_data[2] = Str_sd_user_data[2];
        return ;
      }else  
            Str_sd_user_data[3].toCharArray(select_sdname,Str_sd_user_data[3].length()+1);
            Str_sd_user_data[4].toCharArray(select_sdpswd,Str_sd_user_data[4].length()+1);
          if(strcmp(select_sdname,select_ndefname) == 0 && strcmp(select_sdpswd,select_ndefpwsd) == 0 ){
          Serial.println("User 2 Login ");
           lcdprint("User 2 Login ");
          cardstatus = true ;
          str_ndef_user_data[2] = Str_sd_user_data[5];
          return ;
          }else 
               Str_sd_user_data[6].toCharArray(select_sdname,Str_sd_user_data[6].length()+1);
               Str_sd_user_data[7].toCharArray(select_sdpswd,Str_sd_user_data[7].length()+1);
            if(strcmp(select_sdname,select_ndefname) == 0 && strcmp(select_sdpswd,select_ndefpwsd) == 0 ){
               Serial.println("User 3 Login ");
                lcdprint("User 3 Login ");
               cardstatus = true ;
               str_ndef_user_data[2] = Str_sd_user_data[8];
                 return ;
            }else cardstatus = false; 
             Serial.println("UserName or Password error .. ");
              lcdprint("UserName or Password error .. ");
       }else{
            Serial.println("No User Data , Please create new user.. ");
            lcdprint("No User Data , Please create new user.. ");
           }
         }else {
          Serial.println("No User Data , Please create new user.. ");
          lcdprint("No User Data , Please create new user.. ");
         }
  break;
  // 修正ok !
  case 2 :
        Serial.println("Do the Case 2 IMEI Login ");
        if(sd_user_login_data == true){
           if(userquantity>0){
            if(str_ndef_user_data[2].toInt()==Str_sd_user_data[2].toInt()){
              Serial.println("User 1 Login ");
              lcdprint("User 1 Login ");
              str_ndef_user_data[0] = Str_sd_user_data[0];
              str_ndef_user_data[1] = Str_sd_user_data[1];
              cardstatus = true ;
              return ;
            }else if(str_ndef_user_data[2].toInt()==Str_sd_user_data[5].toInt()){
              Serial.println("User 2 Login ");
              lcdprint("User 2 Login ");
              str_ndef_user_data[0] = Str_sd_user_data[3];
              str_ndef_user_data[1] = Str_sd_user_data[4];
              cardstatus = true ;
              return ;
            }else if(str_ndef_user_data[2].toInt()==Str_sd_user_data[8].toInt()){
              Serial.println("User 3 Login ");
              lcdprint("User 3 Login ");
              str_ndef_user_data[0] = Str_sd_user_data[6];
              str_ndef_user_data[1] = Str_sd_user_data[7];
              cardstatus = true ;
              return ;
            } else cardstatus = false ; 
            Serial.println(" IMEI Number error .. ");
            lcdprint(" IMEI Number error .. ");
            delay(1000);
           } else{
            Serial.println("No User Data , Please create new user.. ");
            lcdprint("No User Data , Please create new user.. ");
           }
         }else {
          Serial.println("No User Data , Please create new user.. ");
          lcdprint("No User Data , Please create new user.. ");
         }     
  break;
  case 3 :  
          Serial.println("Do the Case 3 New User ");
          if(userquantity < 3){ 
             Str_sd_user_data[0].toCharArray(select_sdname,Str_sd_user_data[0].length()+1);
             Str_sd_user_data[2].toCharArray(select_sdpswd,Str_sd_user_data[2].length()+1);
             str_ndef_user_data[0].toCharArray(select_ndefname,str_ndef_user_data[0].length()+1);
             str_ndef_user_data[2].toCharArray(select_ndefpwsd,str_ndef_user_data[2].length()+1);
             if(strcmp(select_sdname,select_ndefname) == 0 && str_ndef_user_data[2].toInt()==Str_sd_user_data[2].toInt() ){
                    newuserpsw = false ;
                   Serial.println(" The User or IMEI are duplicated ");
                    lcdprint(" The User or IMEI are duplicated ");
             }else Str_sd_user_data[3].toCharArray(select_sdname,Str_sd_user_data[3].length()+1);
                  Str_sd_user_data[5].toCharArray(select_sdpswd,Str_sd_user_data[5].length()+1);
                  if(strcmp(select_sdname,select_ndefname) == 0 && str_ndef_user_data[2].toInt()==Str_sd_user_data[5].toInt() ){
                    newuserpsw = false ;
                    Serial.println(" The User or IMEI are duplicated ");
                     lcdprint(" The User or IMEI are duplicated ");
                  }else Str_sd_user_data[6].toCharArray(select_sdname,Str_sd_user_data[6].length()+1);
                      Str_sd_user_data[8].toCharArray(select_sdpswd,Str_sd_user_data[8].length()+1);
                  if(strcmp(select_sdname,select_ndefname) == 0 && str_ndef_user_data[2].toInt()==Str_sd_user_data[8].toInt() ){
                    newuserpsw = false ;
                    Serial.println(" The User or IMEI are duplicated ");
                    lcdprint(" The User or IMEI are duplicated ");
                    return ;
                  }else  newuserpsw = true ;
                  Serial.println(" Add the New User ... ");
                  lcdprint(" Add the New User ... ");
                  return ;
            }else{
            Serial.println(" Not to more User ...");
            lcdprint(" Not to more User ...");
          }
  break;
  case 4 :
          delete_line = 0 ;
          Serial.println("Do the Case 4 Remove User ");
        if(sd_user_login_data == true){
           if(userquantity>0){
             Str_sd_user_data[0].toCharArray(select_sdname,Str_sd_user_data[0].length()+1);
             Str_sd_user_data[1].toCharArray(select_sdpswd,Str_sd_user_data[1].length()+1);
             Str_sd_user_data[2].toCharArray(select_sdimei,Str_sd_user_data[2].length()+1);
             str_ndef_user_data[0].toCharArray(select_ndefname,str_ndef_user_data[0].length()+1);
             str_ndef_user_data[1].toCharArray(select_ndefpwsd,str_ndef_user_data[1].length()+1);
             str_ndef_user_data[2].toCharArray(select_ndefimei,str_ndef_user_data[2].length()+1);
             if(strcmp(select_sdname,select_ndefname) == 0 && strcmp(select_sdpswd,select_ndefpwsd) == 0 && str_ndef_user_data[2].toInt()==Str_sd_user_data[2].toInt() ){
                delete_line = 0 ;
                removeuser = true ;
                lcdprint("Remove user 1");
                return ;
             }else  Str_sd_user_data[3].toCharArray(select_sdname,Str_sd_user_data[3].length()+1);
                   Str_sd_user_data[4].toCharArray(select_sdpswd,Str_sd_user_data[4].length()+1);
                   Str_sd_user_data[5].toCharArray(select_sdimei,Str_sd_user_data[5].length()+1);
                   if(strcmp(select_sdname,select_ndefname) == 0 && strcmp(select_sdpswd,select_ndefpwsd) == 0 && str_ndef_user_data[2].toInt()==Str_sd_user_data[5].toInt() ){
                      delete_line = 1 ;
                     removeuser = true ;
                     lcdprint("Remove user 2");
                     return ;
                   }else Str_sd_user_data[6].toCharArray(select_sdname,Str_sd_user_data[6].length()+1);
                        Str_sd_user_data[7].toCharArray(select_sdpswd,Str_sd_user_data[7].length()+1);
                        Str_sd_user_data[8].toCharArray(select_sdimei,Str_sd_user_data[8].length()+1);
                        if(strcmp(select_sdname,select_ndefname) == 0 && strcmp(select_sdpswd,select_ndefpwsd) == 0 && str_ndef_user_data[2].toInt()==Str_sd_user_data[8].toInt()){
                          delete_line = 2 ;
                          removeuser = true ;
                          lcdprint("Remove user 3");
                          return ;
                        }else  removeuser = false;
                         Serial.println("No User Data , Please Check again ... "); 
                        lcdprint("No User Data , Please Check again ... ");
           } else{
            Serial.println("No User Data , Please Check again ... ");
            lcdprint("No User Data , Please Check again ... ");
           }
         }else {
          Serial.println("No User Data ,  Please Check again ... ");
          lcdprint("No User Data , Please Check again ... ");
         }
  break;
  default:
  Serial.println("Select Error");
  lcdprint("Select Error");
  break;
  }
}
//進行 結果判斷
void Result_judgment(){
    Serial.println("------------Result Judgment------------");
    if (cardstatus == true ){
    digitalWrite(mSwitch,LOW);
    Serial.println("Success to Open ...");
    lcd.setCursor(0,0);
    lcd.print("success to open ...");
    lcd.setCursor(2,1);
    lcd.print(" seconds left");
    for(int j=7;j>=0;j--){
      lcd.setCursor(0,1);
      lcd.print("  ");
      lcd.setCursor(0,1);
      lcd.print(j);
      delay(1000);
    }
    cardstatus==false;
    digitalWrite(mSwitch,HIGH);
    RecordUseruse();
    Lease_Status();
    return ;
    }else if(newuserpsw == true){
        NewUserData();
        RecordUseruse();
        Serial.println("-Sdcard User ReadData and String Split-");
        sdcard_user_readData();
        return;
        }else if(removeuser == true){
            RemoveUser();
            RecordUseruse();
            Serial.println("-Sdcard User ReadData and String Split-");
            sdcard_user_readData();
            return;
            }     
}
//創建新使用帳戶 
void NewUserData(){
  Serial.println(" NewUser ...");
  lcdprint("NewUser ...");
  digitalWrite(NFC_CS,HIGH);
  digitalWrite(SD_CS,LOW);
  SD.remove("USERDATA.txt");
  myFile = SD.open("USERDATA.txt",FILE_WRITE);
  if(myFile){
  for(int i = 0 ; i <= 2 ; i++){
     Str_SDUser[i].trim();
    if(Str_SDUser[i].length()>0){
    myFile.println(Str_SDUser[i]);}}
    DataNC = str_ndef_user_data[0]+","+str_ndef_user_data[1]+","+str_ndef_user_data[2]; //固定格式 username , password , IMEI
    myFile.println(DataNC);
    myFile.close();
    Serial.println(" NewUser ...ok");
  }else {
      Serial.println("error opening USERDATA.txt");
    }
  Serial.println("The New User is ok   ...");
  lcdprint("The New User is ok   ...");
  digitalWrite(SD_CS,HIGH);
  digitalWrite(NFC_CS,LOW);
  return ;
}
//紀錄使用紀錄
void RecordUseruse(){
  DataNC = "";
    Serial.println(" Record User use ...");
    lcdprint("Record User use ...");
    digitalWrite(NFC_CS,HIGH);
    digitalWrite(SD_CS,LOW);
    myFile = SD.open("useruse.txt",FILE_WRITE);
    getTime();
  if (myFile){
    DataNC = str_ndef_user_data[0]+","+str_ndef_user_data[1]+","+str_ndef_user_data[2]+","+Ndef_TNF_Type+" "+times; //固定格式 username , password , IMEI, TNF times
    myFile.println(DataNC);
    myFile.close();
    Serial.println(DataNC);
  }else {
      Serial.println("error opening useruse.txt");
    }
  if(Leasestatus == false){
    myFile = SD.open("LEASE.txt",FILE_WRITE);
    DataNC = str_ndef_user_data[0]+","+str_ndef_user_data[1]+","+str_ndef_user_data[2];
    myFile.println(DataNC);
    myFile.close();
  }else{
    SD.remove("LEASE.txt");
    myFile = SD.open("LEASE.txt",FILE_WRITE);
    myFile.close();
  }
  digitalWrite(SD_CS,HIGH);
  digitalWrite(NFC_CS,LOW);
  return ;
} 
// 刪除使用者
void RemoveUser(){
    digitalWrite(NFC_CS,HIGH);
    digitalWrite(SD_CS,LOW);
    Serial.println(" RemoveUser ...");
    lcdprint(" RemoveUser ...");
    SD.remove("USERDATA.txt");
    myFile = SD.open("USERDATA.txt",FILE_WRITE);
  for(int i = 0 ; i <= 3 ; i++){
    if( i != delete_line ){
      myFile.println(Str_SDUser[i]);
    }
  }
  myFile.close();
  Serial.println(" RemoveUser ... ok");
  lcdprint(" RemoveUser ... ok");
  delay(500);
   digitalWrite(SD_CS,HIGH);
  digitalWrite(NFC_CS,LOW);
  return ;  
}
// sd資料讀取&&處理 ok
void sdcard_user_readData(){
    int commaPosition = 0 ,i = 0 ,z=-1;
    String SDMes ,str_sduser , str_sd_user_data;  
    for(int j=0;j<=3;j++){
      Str_SDUser[j]="";}
    for(int z=0;z<=8;z++){
    Str_sd_user_data[z]="";}
    digitalWrite(SD_CS,LOW);
    digitalWrite(NFC_CS,HIGH);
    myFile = SD.open("USERDATA.txt");    
    userquantity = 0;  
    while (myFile.available() && userquantity < 3) {
          buffer = myFile.readStringUntil('\n');
          str_sduser  = buffer;  
          str_sduser.trim();
          if(str_sduser.length()>0){       
          Str_SDUser[i] = str_sduser;
          i=i+1;    
          userquantity = userquantity + 1 ;    
           }
          buffer.remove(0);  
          delay(100);   
          }  
          myFile.close();  
          digitalWrite(SD_CS,HIGH);
          digitalWrite(NFC_CS,LOW);
          Serial.print(" userquantity : "); Serial.println(userquantity);
          for(int j=0 ;j<= 2;j++){
          SDMes = Str_SDUser[j];
          Serial.print(" SDMes : "); Serial.println(SDMes);
          commaPosition = 0 ;
          do{   
              z=z+1;
               commaPosition = SDMes.indexOf(',');
               Serial.print(" commaPosition : "); Serial.println(commaPosition);    
               if(commaPosition != -1){    
                 str_sd_user_data = SDMes.substring(0,commaPosition);  
                 Str_sd_user_data[z]=str_sd_user_data;  
                 SDMes = SDMes.substring(commaPosition+1,SDMes.length());  
                 ;   
                 }else{    
                      if(SDMes.length()>0){
                        str_sd_user_data = SDMes;
                        Str_sd_user_data[z]= str_sd_user_data;}    
                     }                 
              }  while(commaPosition >= 0) ;      
              }
Serial.println("------------SD Data Print--------------");
Serial.println(" User    : UserName , Password , IMEI");
Serial.print(" User 1  : "); Serial.println(Str_SDUser[0]);
Serial.print(" User 2  : "); Serial.println(Str_SDUser[1]);
Serial.print(" User 3  : "); Serial.println(Str_SDUser[2]);
Serial.print(" User 1 Name : "); Serial.println(Str_sd_user_data[0]);
Serial.print(" User 1 Password : "); Serial.println(Str_sd_user_data[1]);
Serial.print(" User 1 IMEI : "); Serial.println(Str_sd_user_data[2]);
Serial.print(" User 2 Name : "); Serial.println(Str_sd_user_data[3]);
Serial.print(" User 2 Password : "); Serial.println(Str_sd_user_data[4]);
Serial.print(" User 2 IMEI : "); Serial.println(Str_sd_user_data[5]);
Serial.print(" User 3 Name : "); Serial.println(Str_sd_user_data[6]);
Serial.print(" User 3 Password : "); Serial.println(Str_sd_user_data[7]);
Serial.print(" User 3 IMEI : "); Serial.println(Str_sd_user_data[8]);
return ;
}
// ndef 內容切割
void ndef_str_split (String NdefMes){    
      int commaPosition = 0 ,i = -1 ;
      String Str_ndef_user_data;
      if (Ndef_TNF_Type != 2 ){  
            while(commaPosition >= 0){    
               commaPosition = NdefMes.indexOf(',');
                i=i+1;      
               if(commaPosition != -1){    
                 Str_ndef_user_data = NdefMes.substring(0,commaPosition);  
                 str_ndef_user_data[i]=Str_ndef_user_data;  
                 NdefMes = NdefMes.substring(commaPosition+1,NdefMes.length());  
 
              }else{    
                   if(NdefMes.length()>0){    
                   Str_ndef_user_data= NdefMes;   
                   str_ndef_user_data[i]=Str_ndef_user_data;  
                      }    
                  }                 
               }  
       }else  
        {  
          str_ndef_user_data[2] = NdefMes ;  
        }
Serial.println("-----------NDEF Data Print-------------");
Serial.print(" str_ndef_user_data[0] : "); Serial.println(str_ndef_user_data[0]);
Serial.print(" str_ndef_user_data[1] : "); Serial.println(str_ndef_user_data[1]);
Serial.print(" str_ndef_user_data[2] : "); Serial.println(str_ndef_user_data[2]);
Serial.print(" str_ndef_user_data[3] : "); Serial.println(str_ndef_user_data[3]);
Serial.print(" str_ndef_user_data[4] : "); Serial.println(str_ndef_user_data[4]);
   return ; 
}
//Lcd輸出文字
void lcdprint(String lcdtext) {
  lcd.clear();
  if(lcdtext.length()>16)
  { 
    lcd.setCursor(0,0);
    lcd.print(lcdtext.substring(0,16));
    lcd.setCursor(0,1);
    lcd.print(lcdtext.substring(16));
  }
  else
  {
    lcd.setCursor(0,0);
    lcd.print(lcdtext.substring(0,16));
  }
  delay(3000);
}
// 獲取時間
void getTime(){
   DateTime now = rtc.now();
    int y = now.year();
    int m = now.month();
    int d = now.day();
    int h = now.hour();
    int mi = now.minute(); 
    int a[6]={y,m,d,h,mi};
    String str1="// :";
    times.remove(0);
    for(int i =0 ; i<5 ; i++) 
      {
        times=times+a[i]+str1[i];
        delay(100);
      }
    delay(1000);
    return ;
}
