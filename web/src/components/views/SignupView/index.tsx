import SignupForm from "@components/organisms/SignupForm";
import React, { FC } from "react";

interface SignupViewProps {}

const SignupView: FC<SignupViewProps> = () => {
  return (
    <div className="h-screen flex-center bg-red-50">
      <div className="w-full">
        <div className="mt-10 w-full text-center font-bold text-4xl">QHAT</div>
        <div className="mt-2 mb-10 w-full text-center font-semibold text-md text-gray-500">
          채팅의 새로운 기준
        </div>
        <div className="w-full flex-center">
          <SignupForm></SignupForm>
        </div>
      </div>
    </div>
  );
};

export default SignupView;
