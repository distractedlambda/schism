pub const user_led = struct {
    pub const green = 13;
    pub const red = 14;
    pub const blue = 15;
};

pub const spi = struct {
    pub const cs = 5;
    pub const sck = 6;
    pub const mosi = 7;
};

pub const screen = struct {
    pub const lcd_reset = 4;
    pub const vsync = 8;
    pub const dc = 9;
    pub const backlight = 12;
};

pub const buttons = struct {
    pub const y = 16;
    pub const x = 17;
    pub const a = 18;
    pub const b = 19;
    pub const down = 20;
    pub const right = 21;
    pub const left = 22;
    pub const up = 23;
};

pub const battery = struct {
    pub const charge_led = 2;
    pub const charging = 24;
    pub const level = 26;
};

pub const audio = 11;
