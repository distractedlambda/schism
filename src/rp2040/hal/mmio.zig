pub fn read(address: u32) u32 {
    return @intToPtr(*allowzero volatile u32, address).*;
}

pub fn write(address: u32, value: u32) void {
    @intToPtr(*allowzero volatile u32, address).* = value;
}

pub fn peripheralToggle(address: u32, mask: u32) void {
    @intToPtr(*volatile u32, address + 0x1000).* = mask;
}

pub fn peripheralSet(address: u32, mask: u32) void {
    @intToPtr(*volatile u32, address + 0x2000).* = mask;
}

pub fn peripheralClear(address: u32, mask: u32) void {
    @intToPtr(*volatile u32, address + 0x3000).* = mask;
}
