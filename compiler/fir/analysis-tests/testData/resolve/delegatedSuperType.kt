// RUN_PIPELINE_TILL: BACKEND
interface A {
    fun foo()
}

class B : A {
    override fun foo() {}
}

class C(val b: B) : A by b
