const std = @import("std");

pub fn FixedPoint(comptime Significand: type, comptime exponent: comptime_int) type {
    switch (@typeInfo(Significand)) {
        .Int => {},
        else => @compileError("Significand must be a fixed-width integer type"),
    }

    return struct {
        const _Brand = Brand;

        pub const Significand = Significand;

        pub const exponent = exponent;

        significand: Significand,

        pub fn new(significand: Significand) @This() {
            return .{ .significand = significand };
        }

        pub fn withExponent(self: @This(), comptime new_exponent: comptime_int) FixedPoint(Significand, new_exponent) {
            if (new_exponent < exponent) {
                return .{ .significand = self.significand <<| (exponent - new_exponent) };
            } else {
                return .{ .significand = self.significand >> (new_exponent - exponent) };
            }
        }

        pub fn withSignificandAs(self: @This(), comptime NewSignificand: type) FixedPoint(NewSignificand, exponent) {
            return .{ .significand = @as(NewSignificand, self.significand) };
        }

        pub fn add(lhs: @This(), rhs: anytype) (blk: {
            assertIsFixedPoint(rhs);

            if (@TypeOf(rhs).exponent != exponent) {
                @compileError("cannot add FixedPoint types when exponents differ");
            }

            break :blk FixedPoint(@TypeOf(lhs.significand, rhs.significand), exponent);
        }) {
            return .{ .significand = lhs.significand +| rhs.significand };
        }

        pub fn sub(lhs: @This(), rhs: @This()) (blk: {
            assertIsFixedPoint(rhs);

            if (@TypeOf(rhs).exponent != exponent) {
                @compileError("cannot subtract FixedPoint types when exponents differ");
            }

            break :blk FixedPoint(@TypeOf(lhs.significand, rhs.significand), exponent);
        }) {
            return .{ .significand = lhs.significand -| rhs.significand };
        }

        pub fn mul(lhs: @This(), rhs: anytype) (blk: {
            assertIsFixedPoint(rhs);
            break :blk FixedPoint(@TypeOf(lhs.significand, rhs.significand), exponent + @TypeOf(rhs).exponent);
        }) {
            return .{ .significand = lhs.significand *| rhs.significand };
        }

        pub fn div(lhs: @This(), rhs: anytype) (blk: {
            assertIsFixedPoint(rhs);
            break :blk FixedPoint(@TypeOf(lhs.significand, rhs.significand), exponent - @TypeOf(rhs).exponent);
        }) {
            var significand: @TypeOf(lhs.significand, rhs.significand) = undefined;

            if (rhs.significand == 0) {
                if (lhs.significand < 0) {
                    significand = std.math.minInt(@TypeOf(significand));
                } else {
                    significand = std.math.maxInt(@TypeOf(significand));
                }
            } else if (@typeInfo(Significand).signedness == .signed and lhs.significand == std.math.minInt(Significand) and rhs.significand == -1) {
                significand = std.math.maxInt(@TypeOf(significand));
            } else {
                significand = @divTrunc(lhs.significand, rhs.significand);
            }

            return .{ .significand = significand };
        }
    };
}

pub fn new(significand: anytype, comptime exponent: comptime_int) FixedPoint(@TypeOf(significand), exponent) {
    return .{ .significand = significand };
}

const Brand = opaque {};

fn assertIsFixedPoint(x: anytype) void {
    comptime assertIsFixedPointType(@TypeOf(x));
}

fn assertIsFixedPointType(comptime T: type) void {
    if (comptime !isFixedPointType(T)) {
        @compileError("expected FixedPoint type, got " ++ @typeName(T));
    }
}

fn isFixedPointType(comptime T: type) bool {
    return comptime std.meta.activeTag(@typeInfo(T)) == .Struct and @hasDecl(T, "_Brand") and T._Brand == Brand;
}
