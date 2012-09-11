class ExampleController < ApplicationController
  def show
  end

  def query
    @value = params[:selectorator]
  end
end
