extern fn @"llvm.trap"() noreturn;

pub fn trap() noreturn {
    @"llvm.trap"();
}
