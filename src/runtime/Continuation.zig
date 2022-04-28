frame: anyframe,
next: *@This(),
prior: *@This(),

pub fn init(frame: anyframe) @This() {
    return .{ .frame = frame, .next = undefined, .prior = undefined };
}
