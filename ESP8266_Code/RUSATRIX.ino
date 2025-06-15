#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <Adafruit_NeoPixel.h>

#define LED_PIN   4
#define NUM_LEDS  400
#define num_led   20

#ifndef STASSID
#define STASSID "RUSATRIX" // WIFI NAME
#define STAPSK  ""         // WIFI PASSWORD
#endif

const unsigned int localPort = 8888;
char packetBuffer[1200 + 1];
WiFiUDP Udp;

Adafruit_NeoPixel NeoPixels(NUM_LEDS, LED_PIN, NEO_GRB + NEO_KHZ800);

enum Mode { Snake, Text, Paint, Tetris };

Mode matrixMode = Tetris;
//ALL
unsigned long lastTime = 0;
//TextMode
#define MAX_TEXT 10
byte letters[MAX_TEXT][num_led][num_led][3];
unsigned int count_letters = 0;
const unsigned int timeNextMove = 120;
unsigned int delta_text = 0;
unsigned int delta_letters = 0;
bool isShowText = false;
//__GAME__
bool pixels[num_led][num_led];
int score;
const int timeNextFrame = 150;
//SnakeMode
int snake_pixels[NUM_LEDS][2];
int snake_dirs[NUM_LEDS][2];
unsigned int lenght_snake = 0;
int posApple[2];
int last_dir[2];
//TetrisMode
void MoveTetromino(bool isRight);
void RotateTetromino(bool isClockwise);
int tetromino_pixels[4][2];
int center_pos[2];
bool isMoveTetromino = true;
unsigned long lastTimeDraw = 0;
const unsigned long delayTimeDraw = 250;
int data_tetrominos[6][2][2] = {{{1,0},{0,1}},//tetromishki
                                {{1,0},{2,0}},
                                {{1,0},{1,1}},
                                {{-1,1},{0,1}},
                                {{1,0},{-1,1}},
                                {{0,1},{1,1}}};
bool isAutoGame = false;
bool isNoMoves = true;

void setup() {
  NeoPixels.begin();
  WiFi.softAP (STASSID, STAPSK);
  Udp.begin(localPort);
  
  if(matrixMode == Tetris){
    GenerateTetromino();
  }
}

void loop() {
  int packetSize = Udp.parsePacket();
  if (packetSize) {
    int n = Udp.read(packetBuffer, 1200);
    
    if(n == 5) {
      if(packetBuffer[0] > 32){
        matrixMode = Paint;
      }else if(packetBuffer[1] > 32){
        StartSnake();
        matrixMode = Snake;
      }else if(packetBuffer[2] > 32){
        count_letters = 0;
        isShowText = false;
        matrixMode = Text;
      }else if(packetBuffer[3] > 32){
        StartTetris();
        matrixMode = Tetris;
      }
      return;
    }
    isNoMoves = false; isAutoGame = false;
    
    if(matrixMode == Text){
      if(n == 1){
        if(packetBuffer[0] > 32){
          for(int x = 0; x < num_led; x++) {
            for(int y = 0; y < num_led; y++) {
              letters[count_letters][x][y][0] = letters[0][x][y][0];
              letters[count_letters][x][y][1] = letters[0][x][y][1];
              letters[count_letters][x][y][2] = letters[0][x][y][2];
            }
          }
          count_letters++;
          isShowText = true;
        }
      }else{
        SaveLetter(packetBuffer);
      }
    }
    if(matrixMode == Paint && n == 1200){
      //NeoPixels.showColor(CRGB(0, 0, 0));delay(30);
      for(int j = 0; j < n; j+=3){
        NeoPixels.setPixelColor((int)(j / 3), NeoPixels.Color((byte)packetBuffer[j], (byte)packetBuffer[j+1], (byte)packetBuffer[j+2]));
      }
      NeoPixels.show();
    } 
    if(matrixMode == Snake && n == 4) {
      if((byte)packetBuffer[0] > 32 && last_dir[1] != 1){
        snake_dirs[0][0] = 0;
        snake_dirs[0][1] = -1;
      }else if((byte)packetBuffer[3] > 32 && last_dir[0] != -1){
        snake_dirs[0][0] = 1;
        snake_dirs[0][1] = 0;
      }else if((byte)packetBuffer[2] > 32 && last_dir[1] != -1){
        snake_dirs[0][0] = 0;
        snake_dirs[0][1] = 1;
      }else if((byte)packetBuffer[1] > 32 && last_dir[0] != 1){
        snake_dirs[0][0] = -1;
        snake_dirs[0][1] = 0;
      }
    }
    if(matrixMode == Tetris && n == 4) {
      if((byte)packetBuffer[0] > 32){
        RotateTetromino(false);
      }else if((byte)packetBuffer[3] > 32){
        MoveTetromino(true);
      }else if((byte)packetBuffer[2] > 32){
        RotateTetromino(true);
      }else if((byte)packetBuffer[1] > 32){
        MoveTetromino(false);
      }
    }
    return;
  }
  if(matrixMode == Paint){
    
  }else 
  if(matrixMode == Snake){
    if(!SnakeUpdate()){
      GameOver();
      StartSnake();
    }
  }else 
  if(matrixMode == Tetris){
    if(!isMoveTetromino && lastTimeDraw + delayTimeDraw < millis()) {
      lastTimeDraw = millis();
      isMoveTetromino = true;
      DrawTetris();
    }
    if(!UpdateTetris()){
      GameOver();
      StartTetris();
    }
  }else 
  if(matrixMode == Text){
    if(isShowText){
      if(millis() > lastTime + timeNextMove){
        lastTime = millis();
        delta_text++;
        DrawText();
      }
    }else if(count_letters != 0){
    }
  }
}

void CheckTetrisLines(){
  for(int y = 0; y < num_led; y++) {
    bool is_win_line = true;
    for(int x = 0; x < num_led; x++) {
      if(!pixels[x][y]){
        is_win_line = false;
        break;
      }
    }
    if (is_win_line){
      score++;
      for(int ly = y; ly > 0; ly--) {
        for(int x = 0; x < num_led; x++) {
          pixels[x][ly] = pixels[x][ly-1];
        }
      }
    }
  }
  if(score > 255) score = 255;
}

void MoveTetromino(bool isRight){
  center_pos[0] += (int)isRight * 2 - 1;
  for(int i = 0; i < 4; i++) {
    if(!isTetrominoInBorders(i) || pixels[tetromino_pixels[i][0] + center_pos[0]][tetromino_pixels[i][1] + center_pos[1]]){
      center_pos[0] -= (int)isRight * 2 - 1;
      return;
    }
  }
  isMoveTetromino = false;
}

void RotateTetromino(bool isClockwise){
  int tetromino_temp[4][2];
  for(int i = 0; i < 4; i++) {
    tetromino_temp[i][0] = tetromino_pixels[i][1];
    tetromino_temp[i][1] = tetromino_pixels[i][0];
    tetromino_temp[i][(int)isClockwise] = -tetromino_temp[i][(int)isClockwise];
    if (!isTetrominoInBorders(tetromino_temp[i])) return;
    if (isTetrominoInBorders(tetromino_temp[i]) && pixels[tetromino_temp[i][0] + center_pos[0]][tetromino_temp[i][1] + center_pos[1]]) return;
  }
  for(int i = 0; i < 4; i++) {
    tetromino_pixels[i][0] = tetromino_temp[i][0];
    tetromino_pixels[i][1] = tetromino_temp[i][1];
  }
  isMoveTetromino = false;
}

void ClearPixels(){
  for(int y = 0; y < num_led; y++) {
    for(int x = 0; x < num_led; x++) {
      pixels[x][y] = false;
    }
  }
}

void DrawPixels(){
  for(int y = 0; y < num_led; y+=2) {
    for(int x = 0; x < num_led; x++) {
	  if(pixels[x][y]) NeoPixels.setPixelColor(y * num_led + x, NeoPixels.Color(255, 255, 255));
      else NeoPixels.setPixelColor(y * num_led + x, NeoPixels.Color(0, 0, 0));
    }
    for(int x = 0; x < num_led; x++) {
	  if(pixels[(num_led - 1) - x][y + 1]) NeoPixels.setPixelColor((y + 1) * num_led + x, NeoPixels.Color(255, 255, 255));
      else NeoPixels.setPixelColor((y + 1) * num_led + x, NeoPixels.Color(0, 0, 0));
    }
  }
}

void DrawTetris(){
  DrawPixels();
  //Draw Tetromino
  for(int i = 0; i < 4; i++) {
    if (!isTetrominoInBorders(i)) continue;
	NeoPixels.setPixelColor((tetromino_pixels[i][1] + center_pos[1]) * num_led + (((tetromino_pixels[i][1] + center_pos[1]) % 2 == 0) ? (tetromino_pixels[i][0] + center_pos[0]) : ((num_led - 1) - (tetromino_pixels[i][0] + center_pos[0]))), NeoPixels.Color(0, 255, 0));
  }
  NeoPixels.show();
}

void GenerateTetromino(){
  int rand_tetro = random(6);
  center_pos[0] = num_led / 2; center_pos[1] = 0;
  tetromino_pixels[0][0] = 0; tetromino_pixels[0][1] = 0;
  tetromino_pixels[1][0] = -1; tetromino_pixels[1][1] = 0;
  tetromino_pixels[2][0] = data_tetrominos[rand_tetro][0][0]; tetromino_pixels[2][1] = data_tetrominos[rand_tetro][0][1];
  tetromino_pixels[3][0] = data_tetrominos[rand_tetro][1][0]; tetromino_pixels[3][1] = data_tetrominos[rand_tetro][1][1];
}

bool isTetrominoInBorders(int temp_pos[2]){
  if(temp_pos[0] + center_pos[0] < 0 || temp_pos[0] + center_pos[0] >= num_led ||
     temp_pos[1] + center_pos[1] < 0 || temp_pos[1] + center_pos[1] >= num_led) return false;
  return true;
}

bool isTetrominoInBorders(int i){
  if(tetromino_pixels[i][0] + center_pos[0] < 0 || tetromino_pixels[i][0] + center_pos[0] >= num_led ||
     tetromino_pixels[i][1] + center_pos[1] < 0 || tetromino_pixels[i][1] + center_pos[1] >= num_led) return false;
  return true;
}

bool UpdateTetris(){
  if(millis() > lastTime + timeNextFrame){
    lastTime = millis();

    center_pos[1]++;
    if(isAutoGame){
      bool ran_dir = random(2);
      for(int i = 0; i < (int)random(4); i++){
        MoveTetromino(ran_dir);
      }
      if(random(4) == 0){
        RotateTetromino(random(2));
      }
    }
    
    bool is_collision = false;
    for(int i = 0; i < 4; i++) {
      if (tetromino_pixels[i][1] + center_pos[1] >= num_led ||
          isTetrominoInBorders(i) && pixels[tetromino_pixels[i][0] + center_pos[0]][tetromino_pixels[i][1] + center_pos[1]]){
        is_collision = true;
        break;
      }
    }
    if (is_collision){
      center_pos[1]--;
      for(int i = 0; i < 4; i++) {
        if (tetromino_pixels[i][1] + center_pos[1] <= 0) return false;
        if (!isTetrominoInBorders(i)) continue;
        pixels[tetromino_pixels[i][0] + center_pos[0]][tetromino_pixels[i][1] + center_pos[1]] = true;
      }
      GenerateTetromino();
      CheckTetrisLines();
    }
    
    DrawTetris();
    isMoveTetromino = true;
  }
  return true;
}

void StartTetris(){
  ClearPixels();
  score = 0;
  GenerateTetromino();
}

void SaveLetter(char packet[NUM_LEDS * 3]){
  if(count_letters >= MAX_TEXT) count_letters = 0;
  count_letters++;
  for(int x = 0; x < num_led; x++) {
    for(int y = 0; y < num_led; y++) {
      letters[count_letters - 1][x][y][0] = (byte)packet[(y * num_led + x) * 3];
      letters[count_letters - 1][x][y][1] = (byte)packet[(y * num_led + x) * 3 + 1];
      letters[count_letters - 1][x][y][2] = (byte)packet[(y * num_led + x) * 3 + 2];
    }
  }
  for(int x = 0; x < num_led; x++) {
    for(int y = 0; y < num_led; y+=2) {
      NeoPixels.setPixelColor(y * num_led + x, NeoPixels.Color(letters[count_letters - 1][x][y][0], letters[count_letters - 1][x][y][1], letters[count_letters - 1][x][y][2]));
      NeoPixels.setPixelColor((y + 1) * num_led + ((num_led - 1) - x), NeoPixels.Color(letters[count_letters - 1][x][y+1][0], letters[count_letters - 1][x][y+1][1], letters[count_letters - 1][x][y+1][2]));
    }
  }
  NeoPixels.show();
}

void DrawText(){
  if(count_letters <= 1) return;
  
  if(delta_text >= num_led){ delta_letters++; delta_text = 0;}
  if(delta_letters >= (count_letters - 1)){ delta_letters = 0; delta_text = 1; }
  
  for(int x = 0; x < num_led; x++) {
    if(x + delta_text < num_led){
      for(int y = 0; y < num_led; y+=2) {
        NeoPixels.setPixelColor(y * num_led + x, NeoPixels.Color(letters[delta_letters][x + delta_text][y][0],letters[delta_letters][x + delta_text][y][1],letters[delta_letters][x + delta_text][y][2]));
        NeoPixels.setPixelColor((y + 1) * num_led + (num_led - 1 - x), NeoPixels.Color(letters[delta_letters][x + delta_text][y + 1][0],letters[delta_letters][x + delta_text][y + 1][1],letters[delta_letters][x + delta_text][y + 1][2]));
      }
    }else{
      for(int y = 0; y < num_led; y+=2) {
        NeoPixels.setPixelColor(y * num_led + x, NeoPixels.Color(letters[delta_letters + 1][x + delta_text - num_led][y][0], letters[delta_letters + 1][x + delta_text - num_led][y][1], letters[delta_letters + 1][x + delta_text - num_led][y][2]));
        NeoPixels.setPixelColor((y + 1) * num_led + (num_led - 1 - x), NeoPixels.Color(letters[delta_letters + 1][x + delta_text - num_led][y + 1][0], letters[delta_letters + 1][x + delta_text - num_led][y + 1][1], letters[delta_letters + 1][x + delta_text - num_led][y + 1][2]));
      }
    }
  }
  NeoPixels.show();
}

void DrawSnake(){
  DrawPixels();
  NeoPixels.setPixelColor(posApple[1] * num_led + ((posApple[1] % 2 == 0) ? posApple[0] : ((num_led - 1) - posApple[0])), NeoPixels.Color(0, 255, 0));
  NeoPixels.show();
}

void StartSnake(){
  ClearPixels();
  score = 0;
  snake_pixels[0][0] = 10;
  snake_pixels[0][1] = 10;
  snake_dirs[0][0] = 0;
  snake_dirs[0][1] = 1;
  lenght_snake = 1;
  SpawnApple();
  AddSnakePart();
}

void DirMove(){
  last_dir[0] = snake_dirs[0][0];
  last_dir[1] = snake_dirs[0][1];
  for(int i = lenght_snake - 1; i >= 0; i--) {
    snake_dirs[i + 1][0] = snake_dirs[i][0];
    snake_dirs[i + 1][1] = snake_dirs[i][1];
  }
}

void AddSnakePart(){
  if(lenght_snake == NUM_LEDS - 1) return;
  snake_dirs[lenght_snake][0] = snake_dirs[lenght_snake - 1][0];
  snake_dirs[lenght_snake][1] = snake_dirs[lenght_snake - 1][1];
  snake_pixels[lenght_snake][0] = snake_pixels[lenght_snake - 1][0] - snake_dirs[lenght_snake - 1][0];
  snake_pixels[lenght_snake][1] = snake_pixels[lenght_snake - 1][1] - snake_dirs[lenght_snake - 1][1];
  lenght_snake++;
  score++;
}

void SpawnApple(){
  int countFreePixels = NUM_LEDS;
  for(int y = 0; y < num_led; y++) {
    for(int x = 0; x < num_led; x++) {
      if(pixels[x][y]){
        countFreePixels--;
      }
    }
  }
  while(true){
    for(int y = 0; y < num_led; y++) {
      for(int x = 0; x < num_led; x++) {
        if(!pixels[x][y]){
          if(random(countFreePixels) == (countFreePixels - 1)){
            posApple[0] = x;
            posApple[1] = y;
            return;
          }
        }
      }
    }
  }
}

bool SnakeUpdate(){
  if(millis() > lastTime + timeNextFrame){
    lastTime = millis();

    for(int i = 0; i < lenght_snake; i++) {
      pixels[snake_pixels[i][0]][snake_pixels[i][1]] = false;
      if(snake_pixels[i][0] + snake_dirs[i][0] < 0){ 
        snake_pixels[i][0] = num_led;
      }
      if(snake_pixels[i][0] + snake_dirs[i][0] == num_led){ 
        snake_pixels[i][0] = -1;
      }
      if(snake_pixels[i][1] + snake_dirs[i][1] < 0){ 
        snake_pixels[i][1] = num_led;
      }
      if(snake_pixels[i][1] + snake_dirs[i][1] == num_led){ 
        snake_pixels[i][1] = -1;
      }
          
      if(!pixels[snake_pixels[i][0] + snake_dirs[i][0]][snake_pixels[i][1] + snake_dirs[i][1]]){
        snake_pixels[i][0] += snake_dirs[i][0];
        snake_pixels[i][1] += snake_dirs[i][1];
        pixels[snake_pixels[i][0]][snake_pixels[i][1]] = true;
      } else {
        return false;
      }
    }
    
    if(snake_pixels[0][0] == posApple[0] && snake_pixels[0][1] == posApple[1]){
      AddSnakePart();
      SpawnApple();
    }
      
    DirMove();
    DrawSnake();
  }
  return true;
}

void GameOver(){
  for(int i = 0; i < NUM_LEDS; i++) {
    NeoPixels.setPixelColor(i, NeoPixels.Color(255,0,0));
  }
  NeoPixels.show();
  delay(400);
  if(score > 255) score = 255;
  for(int i = 0; i < score; i++) {
    NeoPixels.setPixelColor(i, NeoPixels.Color(255,255,255));
    NeoPixels.show();
    delay(((100 - score) > 50) ? (100 - score) : 50);
  }
  score = 0;
  delay(2000);
  if(isNoMoves){
    isAutoGame = true;
  }
  isNoMoves = true;
}
