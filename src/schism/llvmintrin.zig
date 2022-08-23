extern fn @"llvm.trap"() noreturn;

pub inline fn trap() noreturn {
    @"llvm.trap"();
}
